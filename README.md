# simple http client with java

DO NOT COMPARE THIS WITH CURL OR INSOMNIA OR POSTMAN :)

this is a simple http client for internet engineering course.
it will send string to server by socket in port 80.

learn more about project in `todo.pdf`

## how to run
1. install jdk11+
2. compile with `javac Main.java`
3. run with `java Main`
4. enter request url (only http 1.0 supported. no https)
5. enter method (please use `get` :) )
6. wait for server response.
7. answer would be something like this:

```
==============HttpResponse==========
version   = HTTP/1.1
errorCode = 200
errorDesc = OK
headers   = {Server=gunicorn/19.9.0, Access-Control-Allow-Origin=*, Access-Control-Allow-Credentials=true, Connection=close, Content-Length=248, Date=Sun, 10 Jan 2021 16:45:31 GMT, Content-Type=application/json}
-------body--------
{
  "args": {}, 
  "headers": {
    "Accept": "*/*", 
    "Content-Length": "0", 
    "Host": "httpbin.org", 
    "X-Amzn-Trace-Id": "Root=1-5ffb2f2a-40a054d36ab2677f3214b6a7"
  }, 
  "origin": "5.113.239.236", 
  "url": "http://httpbin.org/get"
}
----end of body----
==========End Of HttpResponse=======
```

8. note that [httpbin](http://httpbin.org/) is a very good site for demonstrating this program features.
