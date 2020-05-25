package com.logmein.dms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.logmein.dms.exception.DocumentNotFoundException;
import com.logmein.dms.exception.ExceptionConstants;
import com.logmein.dms.exception.MalformedRequestException;
import static com.logmein.dms.util.AlphaNumericGenerator.generateNextAlphaNumericId;


/**
 * Servlet implementation class DocumentManagementServlet
 */
@WebServlet("/storage/documents/*")
@MultipartConfig
public class DocumentManagementServlet extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = -3983379249102462200L;

  private static Logger logger = LoggerFactory.getLogger(DocumentManagementServlet.class);

  private Path dirPath;

  public Path getDirPath() {
    return dirPath;
  }

  public void setDirPath(Path dirPath) {
    this.dirPath = dirPath;
  }

  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    //Just to have basic logger quickly without properties file. 
    BasicConfigurator.configure();
    try {
      dirPath = Files.createTempDirectory("logmein_dms");
    } catch (IOException e) {
      logger.error("Unable to create Directory", e);
    }
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    FileInputStream inputStream = null;
    OutputStream outputStream = null;
    try {
      String documentId = pickAndValidateDocumentIdFromURI(request);
      Path filePath = checkDocumentExistence(documentId);
      inputStream = new FileInputStream(filePath.toFile());
      outputStream = response.getOutputStream();
      
      File file = new File(filePath.toString());
      /*
       * Reference for Content-Disposition
       * https://www.codejava.net/java-ee/servlet/java-servlet-download-file-example 
       */
      String headerKey = "Content-Disposition";
      String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
      response.setHeader(headerKey, headerValue);
      response.setContentType("application/octet-stream");
      response.setContentLengthLong(file.length());
      response.setStatus(HttpServletResponse.SC_OK);
      byte[] buffer = new byte[4096];
      int bytesRead = -1;
       
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
       
    } catch (MalformedRequestException e) {
      logger.error(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (DocumentNotFoundException e) {
      logger.error(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } finally {
      if(inputStream != null)
        inputStream.close();
      
      if(outputStream != null)
        outputStream.close();
    }
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    try {
      Part filePart = validateAndGetPartFromRequestBody(request);
      String fileName = filePart.getSubmittedFileName();
      String suffix = fileName.substring(fileName.lastIndexOf('.'));
      String documentId = generateNextAlphaNumericId();
      filePart.write(dirPath.toAbsolutePath() + File.separator + documentId + suffix);
      response.setContentType("text/plain");
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setCharacterEncoding("us-ascii");
      response.getWriter().append(documentId);
    } catch(MalformedRequestException e) {
      logger.error(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
   */
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      Part filePart = validateAndGetPartFromRequestBody(request);
      String documentId = pickAndValidateDocumentIdFromURI(request);
      Path existingFilePath = checkDocumentExistence(documentId);
      String existingFileName = existingFilePath.getFileName().toString();
      String fileName = filePart.getSubmittedFileName();
      String suffix = fileName.substring(fileName.lastIndexOf('.'));
      
   
      // Check if the file extension of input type is same as in the storage.
      if(! existingFileName.substring(existingFileName.lastIndexOf('.')).equalsIgnoreCase(suffix)) { 
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionConstants.MIME_MISMATCH);
      }
      filePart.write(dirPath.toAbsolutePath() + File.separator + documentId + suffix);
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (MalformedRequestException e) {
      logger.error(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (DocumentNotFoundException e) {
      logger.error(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
   */
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      String documentId = pickAndValidateDocumentIdFromURI(request);
      Files.delete(checkDocumentExistence(documentId));
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (MalformedRequestException e) {
      logger.error(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (DocumentNotFoundException e) {
      logger.error(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    }
  }
  
  /**
   * Check if the Document ID is missing in the request URI, If not return the document Id for further operations
   */
  private String pickAndValidateDocumentIdFromURI(HttpServletRequest request) throws IOException {
    String documentIdString = request.getPathInfo();
    if (documentIdString == null || (documentIdString.split("/").length <= 1)) {
      throw new MalformedRequestException(ExceptionConstants.DOCUMENT_ID_MISSING);
    }
    return documentIdString.split("/")[1];
  }

  /**
   * Check for document existence. If found, return the path
   */
  private Path checkDocumentExistence(String documentId) throws IOException {
    Optional<Path> documentPath = Files.walk(Paths.get(dirPath.toUri()))
        .filter(Files::isRegularFile)
        .filter(t -> t.getFileName().toString().substring(0, t.getFileName()
            .toString().lastIndexOf(".")).equals(documentId))
        .findAny();

    if (!documentPath.isPresent()) {
      throw new DocumentNotFoundException(ExceptionConstants.DOCUMENT_NOT_FOUND);
    }
    return documentPath.get();
  }

  /**
   * Validate the request body. No multiple files upload is supported.
   */
  private Part validateAndGetPartFromRequestBody(HttpServletRequest request) throws IOException, ServletException {
    
    List<Part> requestParts = new ArrayList<>(request.getParts());
    if(requestParts.size() > 1) {
      throw new MalformedRequestException(ExceptionConstants.NO_SUPPORT_FOR_MULTIPLE_FILES);
    } else if (requestParts.size() == 0 || requestParts.get(0).getSize() == 0) {
      throw new MalformedRequestException(ExceptionConstants.NO_DOCUMENT_IN_REQUEST_BODY);
    }
    return requestParts.get(0);
  }
  

  @Override
  public void destroy() {
    try {
      Files.walk(Paths.get(dirPath.toUri()))
        .filter(Files::isRegularFile)
        .forEach(file -> {
          try {
            Files.delete(file); // Deleting the individual files inside the folder before deleting the folder
          } catch (IOException e) {
            logger.error("Unable to delete file {} with exception {}", file, e.getMessage());
          }
        });
      Files.delete(dirPath); // Deleting Temporary Folder created by init
    } catch (IOException e) {
      logger.error("Unable to delete Folder {} with exception {}", dirPath, e.getMessage());
    }
    super.destroy();
  }
}
