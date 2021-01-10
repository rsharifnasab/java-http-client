import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

class SimpleURL {
  static final Pattern urlPattern = Pattern.compile(
      "(?i)(http://)?([-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b)([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");

  static final int PORT = 80;

  final Matcher matcher;

  public SimpleURL(String address) {
    this.matcher = urlPattern.matcher(address);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("url is not valid");
    }
  }

  public String host() { return matcher.group(2); }

  public String path() {
    String path = matcher.group(3);
    if (path.isBlank())
      return "/";
    else
      return path;
  }

  public int port() { return PORT; }
}

enum HttpMethod {
  GET,
  POST,
  PUT,
  PATCH,
  DELETE;
}

class HttpRequest {
  public static String x_student_id = null;

  public final HttpMethod method;
  public final SimpleURL url;

  public final Socket socket;

  private final OutputStream outStream;
  private final InputStream inStream;

  private final PrintWriter writer;
  private final Scanner responseSc;

  private String response;
  private boolean sent = false;

  public HttpRequest(SimpleURL url, HttpMethod method) {
    this.url = url;
    this.method = method;
    try {
      this.socket = new Socket(url.host(), url.port());

      this.outStream = socket.getOutputStream();
      this.writer = new PrintWriter(outStream, true);

      this.inStream = socket.getInputStream();
      this.responseSc = new Scanner(inStream);
      responseSc.useDelimiter("\\A");

    } catch (UnknownHostException e) {
      throw new RuntimeException("Server not found", e);
    } catch (IOException e) {
      throw new RuntimeException("I/O error", e);
    }
  }

  public void send() {
    if (sent) {
      throw new RuntimeException("unable to re-send request");
    }
    writer.println(method + " " + url.path() + " HTTP/1.0");
    writer.println("Host: " + url.host() + ":80");
    writer.println("Accept: */*");
    writer.println("Content-Length: 0");
    writer.println("Connection: close");
    if (x_student_id != null)
      writer.println("x-student-id: " + x_student_id);
    writer.println();
    // body
    writer.println();

    this.sent = true;

    if (responseSc.hasNext()) {
      this.response = responseSc.next();
    } else
      throw new RuntimeException("nothing in response");
  }

  public String getStringResponse() { return this.response; }

  public HttpResponse getResponse() { return new HttpResponse(this.response); }

  public void close() {
    try {
      responseSc.close();
      inStream.close();

      writer.close();
      outStream.close();

      socket.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

class HttpResponse {

  public final String version;
  public final String errorCode;
  public final String errorDescription;
  public final Map<String, String> headers = new HashMap<>();

  public final String body;

  public HttpResponse(String text) {

    Scanner sc = new Scanner(text);

    this.version = sc.next();
    this.errorCode = sc.next();
    this.errorDescription = sc.nextLine().trim();

    while (sc.hasNext()) {
      String headerLine = sc.nextLine().trim();
      if (headerLine.isBlank())
        break; // header is over

      String[] headerPair = headerLine.split(": ");
      headers.put(headerPair[0], headerPair[1]);
    }
    sc.useDelimiter("\\A");
    this.body = sc.next();
  }

  public String getContentType() {
    return headers.getOrDefault("Content-Type", "text/plain; charset=UTF-8");
  }

  private static void writeToFile(String content, String filename) {
    try (PrintWriter out = new PrintWriter(filename)) {
      out.println(content);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void handleIfJson() {
    if (getContentType().startsWith("application/json")) {
      writeToFile(this.body, "response.json");
      System.out.println("json wrote to file 'response.json'");
    }
  }

  public void handleIfHtml() {
    if (getContentType().startsWith("text/html")) {
      writeToFile(this.body, "response.html");
      System.out.println("html wrote to file 'response.html'");
    }
  }

  public void handleIfText() {
    if (getContentType().startsWith("text/plain")) {
      System.out.println(this.body);
      System.out.println("===txt printed===");
    }
  }

  public void handleError() {
    if ("200".equals(this.errorCode))
      return;

    System.out.println("error with code: " + this.errorCode +
                       ", message: " + this.errorDescription);
  }

  public void handle() {
    System.err.println(this);

    handleIfJson();
    handleIfHtml();
    handleIfText();

    handleError();
  }

  @Override
  public String toString() {
    return
        "==============HttpResponse==========" +
        "\nversion   = " + version +
        "\nerrorCode = " + errorCode +
        "\nerrorDesc = " + errorDescription +
        "\nheaders   = " + headers +
        "\n-------body--------" +
        "\n" + body.trim() +
        "\n----end of body----" +
        "\n==========End Of HttpResponse=======\n";
  }
}

public class Main {

  public static void main(String[] args) {
    System.setProperty("line.separator", "\r\n");
    Scanner sc = new Scanner(System.in);

    while (true) {
      System.out.println("enter url or command");
      String input = sc.nextLine().trim();
      if ("exit".equals(input)) {
        break;
      } else if ("set-student-id-header".equals(input)) {
        HttpRequest.x_student_id = sc.nextLine().trim();
      } else if ("remove-student-id-header".equals(input)) {
        HttpRequest.x_student_id = null;
      } else {
        SimpleURL url = new SimpleURL(input);
        System.out.println("enter method");
        HttpMethod method =
            HttpMethod.valueOf(sc.nextLine().trim().toUpperCase());

        HttpRequest req = new HttpRequest(url, method);
        req.send();

        System.out.println();
        req.getResponse().handle();

        req.close();
      }
    }
    sc.close();
  }
}
