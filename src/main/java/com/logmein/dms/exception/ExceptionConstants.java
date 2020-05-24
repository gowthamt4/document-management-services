package com.logmein.dms.exception;

public interface ExceptionConstants {

  String DOCUMENT_ID_MISSING = "Document Id is missing in the request URI";
  String DOCUMENT_NOT_FOUND = "Invalid document Id";
  String NO_DOCUMENT_IN_REQUEST_BODY = "No document found in the request body";
  String MIME_MISMATCH = "Not able to modify the file as the input file is of different format";
  String NO_SUPPORT_FOR_MULTIPLE_FILES = "API doesnot support for multiple files";
}
