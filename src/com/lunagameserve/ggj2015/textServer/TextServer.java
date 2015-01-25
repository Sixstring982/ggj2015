package com.lunagameserve.ggj2015.textServer;

import com.sun.xml.internal.ws.client.sei.ResponseBuilder;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;
import org.json.*;
import org.apache.http.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import sun.misc.BASE64Encoder;
import sun.security.ssl.Debug;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by glenn on 1/24/15.
 */
public class TextServer implements Stream {

    private LinkedBlockingDeque<String> _receiveQueue;
    private LinkedBlockingDeque<String> _sendQueue;
    private boolean _isStarted;
    private boolean _isDone;
    Thread _sendThread;
    Thread _recvThread;
    Thread _stdinThread;

    public TextServer() {
        _receiveQueue = new LinkedBlockingDeque<String>();
        _sendQueue = new LinkedBlockingDeque<String>();
    }

    public static void main(String[] args) {
        try {
            TextServer server = new TextServer();
            server.start();
            while(true) {
                String s = server.read(1000);
                if(s != null) {
                    System.err.println(s);
                }
            }
            //server.stop();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void start() {
        if(_isStarted) {
            return;
        }
        _isStarted = true;
        _sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendThread();
            }
        });
        _sendThread.start();
        _recvThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveThread();
            }
        });
        _recvThread.start();
        _stdinThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stdinThread();
            }
        });
        _stdinThread.start();
    }

    public synchronized void stop() {
        if(!_isStarted || _isDone) {
            return;
        }
        _isDone = true;
        try {
            _sendThread.wait();
            _recvThread.wait();
            _stdinThread.wait();
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void write(String str) {
        System.err.println("WRITE " + str);
        _sendQueue.push(str);
    }

    /**
     * Reads a line from this {@link Stream}.
     * @param timeout The timeout period, in milliseconds, that reading from this
     *                {@link Stream} will block the calling thread before returning.
     * @return the {@link String} read from this {@link Stream}, or {@code null}
     *         if the timeout elapses.
     */
    public String read(int timeout) {
        try {
            return _receiveQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch(InterruptedException ex) {
            return null;
        }
    }

    public int linesAvailable() {
        return _receiveQueue.size();
    }

    synchronized boolean IsDone() {
        return _isDone;
    }

    private void stdinThread() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!IsDone()) {
            try {
                /* We need to include a header so we know how to route information */
                _receiveQueue.push(reader.readLine());
            } catch (IOException e) {
                continue;
            }
        }
    }

    private void sendThread() {
        while (!IsDone()) {
            String s = null;
            try {
                s = _sendQueue.poll(1, TimeUnit.SECONDS);
            } catch(InterruptedException ex) {
                continue;
            }
            if(s == null) {
                continue;
            }
            s = s.trim();
            int i = s.indexOf(" ");
            if(i == -1) {
                System.err.println("FAIL SEND SYNTAX1 " + s);
                continue;
            }
            String phoneNumber = s.substring(0,i);
            phoneNumber = phoneNumber.trim();

            if(!Pattern.matches("^[+]([0-9]{11})$", phoneNumber)) {
                System.err.println("FAIL SEND SYNTAX2 " + s);
                continue;
            }

            String message = s.substring(i).trim();
            int tries;
            boolean didSend = false;
            for(tries = 0; !didSend && tries < 3; ++tries) {
                didSend = sendSms(phoneNumber, message);
                if(!didSend) {
                    System.err.println("FAIL SEND " + s);
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException ex) {
                        break;
                    }
                }
            }
            if(didSend) {
                System.err.println("OK SEND " + s);
            }
        }
    }

    class ReceivedMessage implements Comparable {
        public int Index;
        public String From;
        public Date SentDate;
        public String Body;

        @Override
        public int compareTo(Object o) {
            ReceivedMessage other = (ReceivedMessage)o;
            if(other.SentDate.after(SentDate)) {
                return -1;
            } else if(other.SentDate.before(SentDate)) {
                return 1;
            } else if(this.Index < other.Index) {
                return -1;
            } else if(this.Index > other.Index) {
                return 1;
            } else  {
                return 0;
            }
        }
    }

    void receiveThread() {
        Date lastMessageTime = new Date();
        Set<String> messagesThatSecond = new TreeSet<String>();
        while(!IsDone()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                continue;
            }
            JSONObject getMessagesResponse = twilioGetMessages(lastMessageTime);
            if (getMessagesResponse == null) {
                continue;
            }

            List<ReceivedMessage> receivedMessages = new ArrayList<ReceivedMessage>();
            try {
                JSONArray smsMessages = getMessagesResponse.getJSONArray("sms_messages");
                int smsMessageCount = smsMessages.length();

                // Sat, 24 Jan 2015 23:12:47 +0000
                SimpleDateFormat messageDateFormatGmt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                String lastMessageTimeString = messageDateFormatGmt.format(lastMessageTime);

                //System.err.println("LAST "+lastMessageTimeString);
                for (int i = 0; i < smsMessageCount; ++i) {
                    JSONObject smsMessage = smsMessages.getJSONObject(i);
                    String dateSentString = smsMessage.getString("date_sent");
                    Date dateSent = messageDateFormatGmt.parse(dateSentString);
                    String messageSid = smsMessage.getString("sid");
                    String messageBody = smsMessage.getString("body");
                    String messageFrom = smsMessage.getString("from");
                    if (dateSent.before(lastMessageTime)) {
                        //System.err.println("BEFORE "+messageFrom + " " +dateSent+" "+ messageBody);
                        continue;
                    }
                    if (dateSent.after(lastMessageTime)) {
                        //System.err.println("AFTER "+messageFrom + " " +dateSent+" " + messageBody);
                        lastMessageTime = dateSent;
                        messagesThatSecond.clear();
                        messagesThatSecond.add(messageSid);
                    } else if (messagesThatSecond.contains(messageSid)) {
                        //System.err.println("DONE "+messageFrom + " " +dateSent+" " + messageBody);
                        continue;
                    } else {
                        //System.err.println("GOOD "+messageFrom + " " +dateSent+" "+ messageBody);
                        messagesThatSecond.add(messageSid);
                    }
                    ReceivedMessage message = new ReceivedMessage();
                    message.Index = i;
                    message.From = messageFrom;
                    message.Body = messageBody;
                    message.SentDate = dateSent;

                    System.err.println(message.From + " " + messageBody);
                    receivedMessages.add(message);
                }

                Collections.sort(receivedMessages);

            } catch (JSONException ex) {
                ex.printStackTrace();
                continue;
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
                continue;
            }

            for(int i = 0; i < receivedMessages.size(); ++i) {
                ReceivedMessage message = receivedMessages.get(i);
                _receiveQueue.push(message.From+" "+message.Body);
            }
        }
    }

    JSONObject twilioGetMessages(Date minimumDate) {

        String url = "<URL>";
        CloseableHttpClient httpClient = null;
        try {
            url = "https://api.twilio.com/2010-04-01/Accounts/" + TextServerConfig.TwilioUsername + "/SMS/Messages.json";
            url += "?To=" + URLEncoder.encode(TextServerConfig.TwilioPhoneNumber, "UTF-8");

            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("YYYY-MM-DD");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            String dateGmtString = dateFormatGmt.format(minimumDate);
            url += "&DateSent" + URLEncoder.encode(">="+dateGmtString, "UTF-8");

            httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                    TextServerConfig.TwilioUsername,
                    TextServerConfig.TwilioPassword);
            request.addHeader(BasicScheme.authenticate(credentials, "US-ASCII", false));
            HttpResponse response = httpClient.execute(request);
            String responseContentString = IOUtils.toString(response.getEntity().getContent());
            if (response.getStatusLine().getStatusCode() != 200) {
                System.err.println("FAIL GET " + response.getStatusLine().getStatusCode() + " " + url);
                System.err.println(responseContentString);
                return null;
            }
            // System.err.println("SUCCESS GET " + url);

            JSONObject responseContentJson = new JSONObject(responseContentString);
            return responseContentJson;
        } catch (Exception ex) {
            System.err.println("FAIL GET " + url);
            // handle exception here
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    boolean sendSms(String phoneNumber, String message) {
        String url = "<URL>";
        CloseableHttpClient httpClient = null;
        try {
            url = "https://api.twilio.com/2010-04-01/Accounts/" + TextServerConfig.TwilioUsername + "/SMS/Messages.json";

            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("From", TextServerConfig.TwilioPhoneNumber));
            data.add(new BasicNameValuePair("To", phoneNumber));
            data.add(new BasicNameValuePair("Body", message));

            httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(url);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                    TextServerConfig.TwilioUsername,
                    TextServerConfig.TwilioPassword);
            request.addHeader( BasicScheme.authenticate(credentials, "US-ASCII", false) );
            request.setEntity(new UrlEncodedFormEntity(data));
            HttpResponse response = httpClient.execute(request);
            if(response.getStatusLine().getStatusCode() != 201) {
                System.err.println("FAIL POST "+response.getStatusLine().getStatusCode() +" "+url);
                System.err.println(IOUtils.toString(response.getEntity().getContent()));
                return false;
            }
            System.err.println("SUCCESS POST "+url);
            return true;
            // handle response here...
        } catch (Exception ex) {
            System.err.println("FAIL POST "+url);
            // handle exception here
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if(httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
