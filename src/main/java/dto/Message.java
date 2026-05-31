package dto;

/**
 * Data transfer object for WebSocket messages.
 */
public class Message {
  private final String type;
  private final Object data;

  public Message(String type, Object data) {
    this.type = type;
    this.data = data;
  }

  // Gson sẽ dùng getter để serialize, hoặc bạn có thể để public field
  public String getType() {
    return type;
  }

  public Object getData() {
    return data;
  }
}