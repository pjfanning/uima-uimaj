/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.caseditor.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.caseditor.CasEditorPlugin;
import org.apache.uima.caseditor.core.model.DefaultColors;
import org.apache.uima.caseditor.core.model.DocumentElement;
import org.apache.uima.caseditor.core.model.INlpElement;
import org.apache.uima.caseditor.core.model.dotcorpus.DotCorpus;
import org.apache.uima.caseditor.core.model.dotcorpus.DotCorpusSerializer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;

public class DefaultCasDocumentProvider extends
        org.apache.uima.caseditor.editor.CasDocumentProvider {

  /**
   * This map resolved an opened document to its associated style object id.
   * 
   * The tracking is done in the provider because the document element itself
   * does not has any link to the style object.
   */
  private Map<String, String> documentToTypeSystemMap = new HashMap<String, String>();
  
  private Map<String, EditorAnnotationStatus> sharedEditorStatus = new HashMap<String, EditorAnnotationStatus>();
  
  /**
   * This map resolves a type system to a style.
   * 
   * TODO: Right now styles are not closed, how are they
   * deleted when they are not longer needed ?!
   */
  private Map<String, DotCorpus> styles = new HashMap<String, DotCorpus>();
  
  @Override
  protected IDocument createDocument(Object element) throws CoreException {
    if (element instanceof FileEditorInput) {
      FileEditorInput fileInput = (FileEditorInput) element;

      IFile casFile = fileInput.getFile();

      INlpElement nlpElement = CasEditorPlugin.getNlpModel().findMember(casFile);

      // Thats the case if the CAS is inside a Cas Editor Project
      // and part of a corpus folder
      if (nlpElement instanceof DocumentElement) {

        try {
          org.apache.uima.caseditor.editor.ICasDocument workingCopy =
                  ((DocumentElement) nlpElement).getDocument(true);

          AnnotationDocument document = new AnnotationDocument();

          document.setDocument(workingCopy);

          elementErrorStatus.remove(element);

          return document;
        } catch (CoreException e) {
          elementErrorStatus.put(element, new Status(IStatus.ERROR, CasEditorPlugin.ID, IStatus.OK,
                  "There is a problem with the document: " + e.getMessage(), e));
        }
      } else if (CasEditorPlugin.getNlpModel().
              findMember(casFile.getProject()) instanceof INlpElement) {
        IStatus status;

        if (nlpElement == null) {
          status = new Status(IStatus.ERROR, CasEditorPlugin.ID, IStatus.OK,
                  "Document not in a corpus folder!", null);
        } else {
          status = new Status(IStatus.ERROR, CasEditorPlugin.ID, IStatus.OK, "Not a cas document!",
                  null);
        }

        elementErrorStatus.put(element, status);
      }
      // handle the non Cas Editor project case here
      else {

        // Try to find a type system for the CAS file

        // TODO: Discuss logic to resolve type system on dev list
        // For now it will just be assumed that the type system is placed
        // in a file called TypeSystem.xml at the root of the project

        IFile typeSystemFile = casFile.getProject().getFile("TypeSystem.xml");
        
        if (typeSystemFile.exists()) {
          
          // Try to load a style file for the type system
          // Should be named: ts file name, prefixed with .style-
          // If it does not exist, create it when it is changed
          // Creating it after the default is changed means that
          // colors could change completely when the a type is
          // added or removed to the type system
          
          IFile styleFile = casFile.getProject().getFile(".style-TypeSystem.xml");
          
          DotCorpus dotCorpus = styles.get(styleFile.getFullPath().toPortableString());
          
          if (dotCorpus == null) {
            if (styleFile.exists()) {
             InputStream styleFileIn = null;;
             try {
               styleFileIn = styleFile.getContents();
               dotCorpus = DotCorpusSerializer.parseDotCorpus(styleFileIn);
             }
             finally {
               if (styleFileIn != null)
                try {
                  styleFileIn.close();
                } catch (IOException e) {
                  CasEditorPlugin.log(e);
                }
             }
            }
            
            if (dotCorpus == null) {
              dotCorpus = new DotCorpus();
              
              // Initialize colors
              CAS cas = DocumentUimaImpl.getVirginCAS(typeSystemFile);
              TypeSystem ts = cas.getTypeSystem();
              
              Collection<AnnotationStyle> defaultStyles = dotCorpus.getAnnotationStyles();
              
              Collection<AnnotationStyle> newStyles = DefaultColors.assignColors(ts, defaultStyles);
              
              for (AnnotationStyle style : newStyles) {
               dotCorpus.setStyle(style);
              }
            }
            
            styles.put(styleFile.getFullPath().toPortableString(), dotCorpus);
          }
          
          documentToTypeSystemMap.put(casFile.getFullPath().toPortableString(),
                  typeSystemFile.getFullPath().toPortableString());

          // TODO:
          // Preferences are bound to the type system
          // Changed in one place, then it should change in all places
          
          CAS cas = DocumentUimaImpl.getVirginCAS(typeSystemFile);
    
          DocumentFormat documentFormat;
    
          // Which file format to use ?
          if (casFile.getName().endsWith("xmi")) {
            documentFormat = DocumentFormat.XMI;
          } else if (casFile.getName().endsWith("xcas")) {
            documentFormat = DocumentFormat.XCAS;
          } else {
            throw new CoreException(new Status(IStatus.ERROR, "org.apache.uima.dev",
                    "Unkown file format!"));
          }
    
          InputStream casIn = casFile.getContents();

          org.apache.uima.caseditor.editor.ICasDocument doc;

          try {
            doc = new DocumentUimaImpl(cas, casIn, documentFormat);
          } finally {
            try {
              casIn.close();
            } catch (IOException e) {
              // TODO: how to handle this error ?
              // Throw an exception, or continue to display file and
              // leave stream open ?
              e.printStackTrace();
            }
          }

          AnnotationDocument document = new AnnotationDocument();
          document.setDocument(doc);
    
          return document;
        }
        else {
          IStatus status = new Status(IStatus.ERROR, "org.apache.uima.dev", IStatus.OK,
                  "Cannot find type system!\nPlease place a valid type system in this path:\n" +
                  typeSystemFile.getLocation().toOSString(), null);
          
          elementErrorStatus.put(element, status);
        }
      }
      
    }

    return null;
  }

  // Needed for non Cas Editor project case
  @Override
  protected void disposeElementInfo(Object element, ElementInfo info) {
    super.disposeElementInfo(element, info);
    
    // Remove the mapping of document to type system
    if (element instanceof FileEditorInput) {
      FileEditorInput editorInput = (FileEditorInput) element;
      documentToTypeSystemMap.remove(editorInput.getFile().getFullPath().toPortableString());
    }
  }
  
  @Override
  protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document,
          boolean overwrite) throws CoreException {

    fireElementStateChanging(element);

    if (element instanceof FileEditorInput) {
      FileEditorInput fileInput = (FileEditorInput) element;

      IFile file = fileInput.getFile();

      INlpElement nlpElement =
              org.apache.uima.caseditor.CasEditorPlugin.getNlpModel().findMember(file);

      if (nlpElement instanceof DocumentElement) {
        DocumentElement documentElement = (DocumentElement) nlpElement;

        try {
          documentElement.saveDocument();
        } catch (CoreException e) {
          fireElementStateChangeFailed(element);
          throw e;
        }
      } else {
        fireElementStateChangeFailed(element);
        return;
      }
    }

    fireElementDirtyStateChanged(element, false);
  }

  private String getTypesystemId(Object element) {
    if (element instanceof FileEditorInput) {
      FileEditorInput editorInput = (FileEditorInput) element;
      return documentToTypeSystemMap.get(editorInput.getFile().getFullPath().toPortableString());
    }
    
    return null;
  }
  
  // get access to style for element
  private DotCorpus getStyle(Object element) {
      String tsId = getTypesystemId(element);
      
      int lastSlashIndex = tsId.lastIndexOf("/");
      
      String styleId = tsId.substring(0, lastSlashIndex + 1);
      styleId = styleId + ".style-" + tsId.substring(lastSlashIndex + 1);
      
      return styles.get(styleId);
  }
  
  private INlpElement getNlpElement(Object element) {
    if (element instanceof FileEditorInput) {
      FileEditorInput fileInput = (FileEditorInput) element;

      IFile file = fileInput.getFile();

      return CasEditorPlugin.getNlpModel().findMember(file);
    }

    return null;
  }

  private void saveStyles(Object element) {
    String tsId = getTypesystemId(element);
    
    int lastSlashIndex = tsId.lastIndexOf("/");
    
    String styleId = tsId.substring(0, lastSlashIndex + 1);
    styleId = styleId + ".style-" + tsId.substring(lastSlashIndex + 1);
    
    DotCorpus style = styles.get(styleId);
    
    // serialize ... 
    IFile dotCorpusFile = ResourcesPlugin.getWorkspace().getRoot().getFile(
            Path.fromPortableString(styleId));
    
    ByteArrayOutputStream dotCorpusBytes = new ByteArrayOutputStream();
    
    try {
      DotCorpusSerializer.serialize(style, dotCorpusBytes);
    } catch (CoreException e) {
      // will not fail, writing to memory
      CasEditorPlugin.log(e);
    }
    
    try {
      if (dotCorpusFile.exists()) {
        dotCorpusFile.setContents(new ByteArrayInputStream(dotCorpusBytes.toByteArray()),
                true, false, null);
      }
      else {
        dotCorpusFile.create(new ByteArrayInputStream(dotCorpusBytes.toByteArray()),
                true, null);
      }
    }
    catch (CoreException e) {
      // might fail if writing is not possible
      // for some reason
      CasEditorPlugin.log(e);
    }
  }
  
  @Override
  public AnnotationStyle getAnnotationStyle(Object element, Type type) {
    
    if (type == null)
    	throw new IllegalArgumentException("type parameter must not be null!");
    
    INlpElement nlpElement = getNlpElement(element);

    if (nlpElement != null) {
      return nlpElement.getNlpProject().getDotCorpus().getAnnotation(type);
    }
    else {
      DotCorpus dotCorpus = getStyle(element);
      
      return dotCorpus.getAnnotation(type);
    }
  }

  @Override
  public void setAnnotationStyle(Object element, AnnotationStyle style) {
    INlpElement nlpElement = getNlpElement(element);

    if (nlpElement != null) {
      nlpElement.getNlpProject().getDotCorpus().setStyle(style);
    }
    else {
      DotCorpus dotCorpus = getStyle(element);
      dotCorpus.setStyle(style);
      
      saveStyles(element); 
    }
  }
  
  @Override
  protected Collection<String> getShownTypes(Object element) {
    INlpElement nlpElement = getNlpElement(element);

    if (nlpElement != null) {
      return nlpElement.getNlpProject().getDotCorpus().getShownTypes();
    }
    else {
      DotCorpus dotCorpus = getStyle(element);
      
      return dotCorpus.getShownTypes();
    }
  }
  
  @Override
  protected void addShownType(Object element, Type type) {
    INlpElement nlpElement = getNlpElement(element);
    
    if (nlpElement != null) {
      nlpElement.getNlpProject().getDotCorpus().addShownType(type.getName());
      
      try {
        nlpElement.getNlpProject().getDotCorpus().serialize();
      } catch (CoreException e) {
        CasEditorPlugin.log(e);
      }
    }
    else {
      DotCorpus dotCorpus = getStyle(element);
      
      dotCorpus.setShownType(type.getName());
    }
  }
  
  @Override
  protected void removeShownType(Object element, Type type) {
    INlpElement nlpElement = getNlpElement(element);

    if (nlpElement != null) {
      nlpElement.getNlpProject().getDotCorpus().removeShownType(type.getName());
      
      try {
        nlpElement.getNlpProject().getDotCorpus().serialize();
      } catch (CoreException e) {
        CasEditorPlugin.log(e);
      }
    }
    else {
      DotCorpus dotCorpus = getStyle(element);
      
      dotCorpus.removeShownType(type.getName());
    }
  }
  
  @Override
  protected EditorAnnotationStatus getEditorAnnotationStatus(Object element) {
    INlpElement nlpElement = getNlpElement(element);

    if (nlpElement != null) {
      return nlpElement.getNlpProject().getEditorAnnotationStatus();
    }
    else {
      EditorAnnotationStatus status = sharedEditorStatus.get(getTypesystemId(element));
      
      if (status == null)
        status = new EditorAnnotationStatus(CAS.TYPE_NAME_ANNOTATION, null);
      
      return status;
    }
  }

  @Override
  protected void setEditorAnnotationStatus(Object element,
          EditorAnnotationStatus editorAnnotationStatus) {
    INlpElement nlpElement = getNlpElement(element);

    if (nlpElement != null) {
      nlpElement.getNlpProject().setEditorAnnotationStatus(editorAnnotationStatus);
    }
    else {
      sharedEditorStatus.put(getTypesystemId(element), editorAnnotationStatus);
    }
  }
}
