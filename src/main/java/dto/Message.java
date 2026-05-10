package dto;

public class Message {
    final private String type;
    final private Object data;   // Có thể là String, Map, List, hoặc bất kỳ object nào

    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    // Gson sẽ dùng getter để serialize, hoặc bạn có thể để public field
    public String getType() { return type; }
    public Object getData() { return data; }
}