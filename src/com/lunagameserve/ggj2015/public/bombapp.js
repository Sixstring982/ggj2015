var express = require('express');
var app = express();
var port = process.env.PORT || 8080;
app.use(express.static(__dirname));
app.get('*', function(req, res) {
	res.sendFile(__dirname); // load our public/index.html file
});
app.listen(port);
console.log('Server running at http://localhost:8080');
exports = module.exports = app;