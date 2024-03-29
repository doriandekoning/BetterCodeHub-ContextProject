package com.benine.backend.http.camerahandlers;

import com.benine.backend.camera.Camera;
import com.benine.backend.http.HTTPServer;

import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CameraInfoHandler extends CameraRequestHandler {

  /**
   * Map containing the handlers, (route, handler).
   */
  private Map<String, CameraRequestHandler> handlers;

  /**
   * Constructor for a new CameraInfoHandler, handling the /camera/ request.
   * @param httpserver for this handler.
   */
  public CameraInfoHandler(HTTPServer httpserver) {
    super(httpserver);
    this.handlers = new HashMap<>();

    addHandler("mjpeg", new CameraStreamHandler(httpserver));
    addHandler("focus", new CameraFocusHandler(httpserver));
    addHandler("move", new CameraMovingHandler(httpserver));
    addHandler("iris", new CameraIrisHandler(httpserver));
    addHandler("zoom", new CameraZoomHandler(httpserver));
    addHandler("inuse", new SetCameraInUseHandler(httpserver));
  }


  @Override
  public void handle(String s, Request request, HttpServletRequest req, HttpServletResponse res)
          throws IOException, ServletException {

    boolean routed = false;
    String route = getRoute(request);

    if (checkValidCameraID(request) && handlers.containsKey(route)) {
      Camera cam = getCameraController().getCameraById(getCameraId(request));
      CameraRequestHandler handler = handlers.get(route);

      if (handler.isAllowed(cam)) {
        handler.handle(s, request, req, res);
        routed = true;
      }
    }
    
    if (!routed) {
      String cameraInfo = getCameraController().getCamerasJSON();
      respond(request, res, cameraInfo);
      request.setHandled(true);
    }
  }

  /**
   * Checks if the camera id of this request is valid.
   * @param request   The current request.
   * @return          True if valid, False if invalid.
   */
  private boolean checkValidCameraID(Request request) {
    int id = getCameraId(request);
    Camera camera = getCameraController().getCameraById(id);

    return id > 0 && camera != null;
  }

  /**
   * Adds a handler to this cameraInfoHandler.
   * @param uri The endpoint location to add the handler.
   * @param handler a handler object.
   */
  public void addHandler(String uri, CameraRequestHandler handler) {
    handlers.put(uri, handler);
  }


  @Override
  boolean isAllowed(Camera cam) {
    return cam != null;
  }
}
