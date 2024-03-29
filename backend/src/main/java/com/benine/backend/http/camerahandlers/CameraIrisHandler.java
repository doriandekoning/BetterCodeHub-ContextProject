package com.benine.backend.http.camerahandlers;

import com.benine.backend.LogEvent;
import com.benine.backend.camera.Camera;
import com.benine.backend.camera.CameraBusyException;
import com.benine.backend.camera.CameraConnectionException;
import com.benine.backend.camera.IrisCamera;
import com.benine.backend.http.HTTPServer;

import org.eclipse.jetty.server.Request;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created on 21-05-16.
 */
public class CameraIrisHandler extends CameraRequestHandler {

  /**
   * Constructs the camera iris handler for this server.
   * @param httpserver this handler is created for.
   */
  public CameraIrisHandler(HTTPServer httpserver) {
    super(httpserver);
  }

  @Override
  public void handle(String s, Request request, HttpServletRequest req, HttpServletResponse res)
          throws IOException, ServletException {
    int camID = getCameraId(request);

    IrisCamera irisCam =  (IrisCamera) getCameraController().getCameraById(camID);
    String autoOn = request.getParameter("autoIrisOn");
    String setPos = request.getParameter("position");
    String speed = request.getParameter("speed");
    Boolean succes = true;
    try {
      setIris(irisCam, autoOn, setPos, speed);
    } catch (CameraConnectionException e) {
      getLogger().log("Cannot connect to camera: " + irisCam.getId(), LogEvent.Type.WARNING);
      succes = false;
    } catch (NumberFormatException e) {
      getLogger().log(e.toString(), LogEvent.Type.WARNING);
      succes = false;
    } catch (CameraBusyException e) {
      getLogger().log("Trying to move busy camera with id: " + camID, LogEvent.Type.WARNING);
    }
    respond(request, res, succes);
    request.setHandled(true);
  }

  /**
   * Sets the iris of the supplied camera.
   * @param iriscam   An IrisCamera
   * @param autoOn    The autoOn parameter
   * @param setPos    The setPos parameter
   * @param speed     The speed of the focus movement.
   * @throws CameraConnectionException if the camera cannot be reached.
   * @throws CameraBusyException        If the camera is busy.
   */
  private void setIris(IrisCamera iriscam,
                       String autoOn, String setPos, String speed)
          throws CameraConnectionException, CameraBusyException {
    if (autoOn != null) {
      boolean autoOnBool = Boolean.parseBoolean(autoOn);
      iriscam.setAutoIrisOn(autoOnBool);
    }
    if (setPos != null) {
      iriscam.setIrisPosition(Integer.parseInt(setPos));
    } else if (speed != null) {
      iriscam.moveIris(Integer.parseInt(speed));
    }
  }

  @Override
  boolean isAllowed(Camera cam) {
    return cam instanceof IrisCamera;
  }

}
