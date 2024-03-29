package com.benine.backend.http;

import com.benine.backend.Config;
import com.benine.backend.Logger;
import com.benine.backend.camera.CameraBusyException;
import com.benine.backend.preset.PresetController;
import com.benine.backend.camera.CameraController;
import com.benine.backend.performance.PresetQueueController;
import com.benine.backend.video.StreamController;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiMap;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;


/**
 * Created on 4-5-16.
 */
public abstract class RequestHandlerTest {
  
  Logger logger = mock(Logger.class);
  Config config = mock(Config.class);
  private RequestHandler handler;

  protected PrintWriter out;
  protected String target;
  protected Request requestMock;
  protected CameraController cameraController = mock(CameraController.class);
  protected StreamController streamController = mock(StreamController.class);
  protected PresetController presetController = mock(PresetController.class);
  protected PresetQueueController presetQueueController = mock(PresetQueueController.class);
  protected HttpServletResponse httpresponseMock;
  protected HttpServletRequest httprequestMock;
  protected HTTPServer httpserver = mock(HTTPServer.class);


  public abstract RequestHandler supplyHandler();

  @Before
  public void initialize() throws IOException,CameraBusyException {
    when(httpserver.getLogger()).thenReturn(logger);
    when(config.getValue("stream_compression")).thenReturn("true");
    when(httpserver.getConfig()).thenReturn(config);
    when(httpserver.getCameraController()).thenReturn(cameraController);
    when(httpserver.getPresetController()).thenReturn(presetController);
    when(httpserver.getStreamController()).thenReturn(streamController);
    when(httpserver.getPresetQueueController()).thenReturn(presetQueueController);
    
    handler = supplyHandler();
    
    out = mock(PrintWriter.class);
    target = "target";

    requestMock = mock(Request.class);
    httpresponseMock = mock(HttpServletResponse.class);
    httprequestMock = mock(HttpServletRequest.class);

    when(httpresponseMock.getWriter()).thenReturn(out);
  }

  public RequestHandler getHandler() {
    return handler;
  }

  public void setPath(String path) {
    when(requestMock.getPathInfo()).thenReturn(path);
  }

  public void setParameters(MultiMap<String> parameters) {
    requestMock.setQueryParameters(parameters);

    for (String s : parameters.keySet()) {
      when(requestMock.getParameter(s)).thenReturn(parameters.getString(s));
    }

    when(requestMock.getQueryParameters()).thenReturn(parameters);

  }

  @Test
  public final void testRespondStatus() throws IOException {
    String response = "response";
    handler.respond(requestMock, httpresponseMock, response);
    verify(httpresponseMock).setStatus(200);
  }

  @Test
  public final void testRespondStatusError() throws IOException {
    String response = "response";
    when(httpresponseMock.getWriter()).thenThrow(new IOException());

    handler.respond(requestMock, httpresponseMock, response);
    verify(httpresponseMock).setStatus(500);
  }

  @Test
  public final void testRespond() throws IOException {
    String response = "response";
    handler.respond(requestMock, httpresponseMock, response);
    verify(out).write(response);
    verify(out).close();
  }

  @Test
  public void testResponseMessageTrueStatus() throws Exception {
    handler.respond(requestMock, httpresponseMock, true);
    verify(httpresponseMock).setStatus(200);
  }
  
  @Test
  public void testResponseMessageFalseStatus() throws Exception {
    handler.respond(requestMock, httpresponseMock, false);
    verify(httpresponseMock).setStatus(200);
  }

  @Test
  public void testResponseMessageTrueMessage() throws Exception {
    String response = "{\"succes\":\"true\"}";
    handler.respond(requestMock, httpresponseMock, true);
    verify(out).write(response);
  }

  @Test
  public void testResponseMessageFalseMessage() throws Exception {
    String response = "{\"succes\":\"false\"}";
    handler.respond(requestMock, httpresponseMock, false);
    verify(out).write(response);
  }
}
