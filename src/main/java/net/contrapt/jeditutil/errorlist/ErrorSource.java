/*
 * Copyright (C) 1999 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.contrapt.jeditutil.errorlist;

import org.gjt.sp.jedit.*;

import net.contrapt.jeditutil.errorlist.DefaultErrorSource.DefaultError;

import java.util.Vector;

/**
 * A named error source.
 * 
 * @author Slava Pestov
 * @version $Id: ErrorSource.java 1136 2006-01-12 22:14:18Z ezust $
 */
public abstract class ErrorSource implements EBComponent {

   /**
    * Registers an error source.
    * @param errorSource The error source
    */
   public static void registerErrorSource(ErrorSource errorSource)
   {
      if(errorSource.registered)
         return;

      synchronized(errorSources)
      {
         errorSources.addElement(errorSource);
         errorSource.registered = true;
         cachedErrorSources = null;
         EditBus.send(new ErrorSourceUpdate(errorSource,
            ErrorSourceUpdate.ERROR_SOURCE_ADDED,null));
      }
   }

   /**
    * Unregisters an error source.
    * @param errorSource The error source
    */
   public static void unregisterErrorSource(ErrorSource errorSource)
   {
      if(!errorSource.registered)
         return;

      EditBus.removeFromBus(errorSource);

      synchronized(errorSources)
      {
         errorSources.removeElement(errorSource);
         errorSource.registered = false;
         cachedErrorSources = null;
         EditBus.send(new ErrorSourceUpdate(errorSource,
            ErrorSourceUpdate.ERROR_SOURCE_REMOVED,null));
      }
   }

   /**
    * Returns an array of registered error sources.
    */
   public static ErrorSource[] getErrorSources()
   {
      synchronized(errorSources)
      {
         if(cachedErrorSources == null)
         {
            cachedErrorSources = new ErrorSource[
               errorSources.size()];
            errorSources.copyInto(cachedErrorSources);
         }
         return cachedErrorSources;
      }
   }
   
   /**
    * This should be abstract but I do not want to
    * break existing plugins.
    * 
    * @since jedit 4.3pre3
    */
   public void addError(final DefaultError error) {}
   
   /**
    * An error.
    */
   public static final int ERROR = 0;

   /**
    * A warning.
    */
   public static final int WARNING = 1;

   /**
    * Returns a string description of this error source.
    */
   public abstract String getName();

   /**
    * Returns the number of errors in this source.
    */
   public abstract int getErrorCount();

   /**
    * Returns an array of all errors in this error source.
    */
   public abstract Error[] getAllErrors();

   /**
    * Returns the number of errors in the specified file.
    * @param path Full path name
    */
   public abstract int getFileErrorCount(String path);

   /**
    * Returns all errors in the specified file.
    * @param path Full path name
    */
   public abstract Error[] getFileErrors(String path);

   /**
    * Returns all errors in the specified line range.
    * @param path The file path
    * @param startLineIndex The line number
    * @param endLineIndex The line number
    * @since ErrorList 1.3
    */
   public abstract ErrorSource.Error[] getLineErrors(String path,
      int startLineIndex, int endLineIndex);


   // unregistered error sources do not fire events.
   // the console uses this fact to 'batch' multiple errors together
   // for improved performance
   protected boolean registered;

   private static Vector errorSources = new Vector();
   private static ErrorSource[] cachedErrorSources;

   /**
    * An error.
    */
   public interface Error
   {
      /**
       * Returns the error type (error or warning)
       */
      int getErrorType();

      /**
       * Returns the source of this error.
       */
      ErrorSource getErrorSource();

      /**
       * Returns the buffer involved, or null if it is not open.
       */
      Buffer getBuffer();

      /**
       * Returns the file path name involved.
       */
      String getFilePath();

      /**
       * Returns just the name portion of the file involved.
       */
      String getFileName();

      /**
       * Returns the line number.
       */
      int getLineNumber();

      /**
       * Returns the start offset.
       */
      int getStartOffset();

      /**
       * Returns the end offset.
       */
      int getEndOffset();

      /**
       * Returns the error message.
       */
      String getErrorMessage();

      /**
       * Returns the extra error messages.
       */
      String[] getExtraMessages();
   }
}
