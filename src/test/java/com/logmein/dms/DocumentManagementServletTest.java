package com.logmein.dms;

import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.logmein.dms.exception.ExceptionConstants;

public class DocumentManagementServletTest {

  @Mock
  private HttpServletRequest request;
  
  @Mock
  private Part part;
  
  @Mock
  Path dirPath ;
  
  @Mock
  private HttpServletResponse response;
  
  @InjectMocks 
  DocumentManagementServlet servlet;
   
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    BasicConfigurator.configure();
  }
  
  @Test
  public void missingFileInPartWhilePost() throws IOException, ServletException {
   List<Part> mockParts = new ArrayList<>(); 
   mockParts.add(part);
   Mockito.when(request.getParts()).thenReturn(mockParts);
   servlet.doPost(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.NO_DOCUMENT_IN_REQUEST_BODY);
  }
  
  @Test
  public void missingPartWhilePost() throws IOException, ServletException {
   List<Part> mockParts = new ArrayList<>(); 
   Mockito.when(request.getParts()).thenReturn(mockParts);
   servlet.doPost(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.NO_DOCUMENT_IN_REQUEST_BODY);
  }
  
  @Test
  public void moreThanOnePartWhilePost() throws IOException, ServletException {
   List<Part> mockParts = new ArrayList<>(); 
   mockParts.add(part);
   mockParts.add(part);
   Mockito.when(request.getParts()).thenReturn(mockParts);
   servlet.doPost(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.NO_SUPPORT_FOR_MULTIPLE_FILES);
  }
  
  @Test
  public void missingFileInPartWhilePut() throws IOException, ServletException {
   List<Part> mockParts = new ArrayList<>(); 
   mockParts.add(part);
   Mockito.when(request.getParts()).thenReturn(mockParts);
   servlet.doPut(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.NO_DOCUMENT_IN_REQUEST_BODY);
  }
  
  @Test
  public void missingPartWhilePut() throws IOException, ServletException {
   List<Part> mockParts = new ArrayList<>(); 
   Mockito.when(request.getParts()).thenReturn(mockParts);
   servlet.doPut(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.NO_DOCUMENT_IN_REQUEST_BODY);
  }
  
  @Test
  public void moreThanOnePartWhilePut() throws IOException, ServletException {
   List<Part> mockParts = new ArrayList<>(); 
   mockParts.add(part);
   mockParts.add(part);
   Mockito.when(request.getParts()).thenReturn(mockParts);
   servlet.doPut(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.NO_SUPPORT_FOR_MULTIPLE_FILES);
  }
  
  @Test
  public void documentIdMissingWhileDelete() throws IOException, ServletException {
   servlet.doDelete(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.DOCUMENT_ID_MISSING);
  }
  
  @Test
  public void documentIdMissingWhileGet() throws IOException, ServletException {
   servlet.doGet(request, response);
   Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.DOCUMENT_ID_MISSING);
  }
  
  @Test
  public void documentIdMissingWhilePut() throws IOException, ServletException {
    List<Part> mockParts = new ArrayList<>();
    mockParts.add(part);
    Mockito.when(request.getParts()).thenReturn(mockParts);
    Mockito.when(part.getSize()).thenReturn(1L);
    servlet.doPut(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.DOCUMENT_ID_MISSING);
  }
  
  @Test
  public void documentNotFoundWhileGet() throws IOException, ServletException {
    Path directoryPath = Files.createTempDirectory("logmein_dms");
    Mockito.when(request.getPathInfo()).thenReturn("/5kPbqhGCRBDH5PKraEfP");
    Mockito.when(dirPath.toUri()).thenReturn(directoryPath.toUri());
    servlet.doGet(request, response);
    Files.delete(directoryPath);
    Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_NOT_FOUND, ExceptionConstants.DOCUMENT_NOT_FOUND);
  }
  
  @Test
  public void documentNotFoundWhileDelete() throws IOException, ServletException {
    Path directoryPath = Files.createTempDirectory("logmein_dms");
    Mockito.when(request.getPathInfo()).thenReturn("/5kPbqhGCRBDH5PKraEfP");
    Mockito.when(dirPath.toUri()).thenReturn(directoryPath.toUri());
    servlet.doDelete(request, response);
    Files.delete(directoryPath);
    Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_NOT_FOUND, ExceptionConstants.DOCUMENT_NOT_FOUND);
  }
  
  @Test
  public void documentNotFoundWhilePut() throws IOException, ServletException {
    List<Part> mockParts = new ArrayList<>();
    mockParts.add(part);
    Mockito.when(request.getParts()).thenReturn(mockParts);
    Mockito.when(part.getSize()).thenReturn(1L);
    Path directoryPath = Files.createTempDirectory("logmein_dms");
    Mockito.when(request.getPathInfo()).thenReturn("/5kPbqhGCRBDH5PKraEfP");
    Mockito.when(dirPath.toUri()).thenReturn(directoryPath.toUri());
    servlet.doPut(request, response);
    Files.delete(directoryPath);
    Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_NOT_FOUND, ExceptionConstants.DOCUMENT_NOT_FOUND);
  }

  /*
   * @Test public void documentUploadWhilePost() throws IOException, ServletException { Path
   * directoryPath = Files.createTempDirectory("logmein_dms"); List<Part> mockParts = new
   * ArrayList<>(); mockParts.add(part); Mockito.when(request.getParts()).thenReturn(mockParts);
   * Mockito.when(part.getSize()).thenReturn(1L);
   * Mockito.when(part.getSubmittedFileName()).thenReturn("test.pdf");
   * 
   * servlet.doPost(request, response); Files.delete(directoryPath);
   * assertEquals(HttpServletResponse.SC_CREATED, response.getStatus()); }
   */
  
}
