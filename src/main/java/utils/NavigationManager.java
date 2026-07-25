package utils;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle UI navigation and scene switching in JavaFX.
 */
public class NavigationManager {

  private static final Logger logger =
      LoggerFactory.getLogger(NavigationManager.class);

  private NavigationManager() { }

  /**
   * Switches the scene using an ActionEvent or Event source.
   *
   * @param event the event triggering the navigation
   * @param fxmlPath the resource path to the FXML file
   */
  public static void navigateTo(Event event, String fxmlPath) {
    navigateTo(event, fxmlPath, null);
  }

  /**
   * Switches the scene and passes data to the target controller via a consumer.
   *
   * @param <T> the controller type
   * @param event the event triggering the navigation
   * @param fxmlPath the resource path to the FXML file
   * @param controllerInitializer consumer to configure the target controller before display
   */
  public static <T> void navigateTo(
      Event event, String fxmlPath, Consumer<T> controllerInitializer) {
    try {
      FXMLLoader loader = new FXMLLoader(
          NavigationManager.class.getResource(fxmlPath));
      Parent root = loader.load();

      if (controllerInitializer != null) {
        T controller = loader.getController();
        if (controller != null) {
          controllerInitializer.accept(controller);
        }
      }

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(new Scene(root));
      stage.show();
    } catch (IOException e) {
      logger.error("Không thể mở màn hình: {}", fxmlPath, e);
      AlertUtils.showError("Lỗi hệ thống", "Không thể chuyển đến màn hình: " + fxmlPath);
    }
  }
}
