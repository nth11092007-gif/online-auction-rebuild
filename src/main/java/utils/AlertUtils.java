package utils;

import javafx.scene.control.Alert;

/**
 * Utility class for displaying standardized JavaFX Alert dialogs.
 */
public class AlertUtils {

  private AlertUtils() { }

  /** Displays an information alert dialog. */
  public static void showInfo(String title, String content) {
    showAlert(title, content, Alert.AlertType.INFORMATION);
  }

  /** Displays an information alert dialog with default title. */
  public static void showInfo(String content) {
    showAlert("Thông báo", content, Alert.AlertType.INFORMATION);
  }

  /** Displays a warning alert dialog. */
  public static void showWarning(String title, String content) {
    showAlert(title, content, Alert.AlertType.WARNING);
  }

  /** Displays a warning alert dialog with default title. */
  public static void showWarning(String content) {
    showAlert("Cảnh báo", content, Alert.AlertType.WARNING);
  }

  /** Displays an error alert dialog. */
  public static void showError(String title, String content) {
    showAlert(title, content, Alert.AlertType.ERROR);
  }

  /** Displays an error alert dialog with default title. */
  public static void showError(String content) {
    showAlert("Lỗi", content, Alert.AlertType.ERROR);
  }

  /** General alert display helper. */
  public static void showAlert(String title, String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
