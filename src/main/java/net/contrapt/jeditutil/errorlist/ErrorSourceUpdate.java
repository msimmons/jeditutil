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

/**
 * Message that an error source has changed.
 *
 * @author Slava Pestov
 */
public class ErrorSourceUpdate extends EBMessage {
   /**
    * An error source has been added.
    */
   public static final Object ERROR_SOURCE_ADDED = "ERROR_SOURCE_ADDED";

   /**
    * An error source has been removed.
    */
   public static final Object ERROR_SOURCE_REMOVED = "ERROR_SOURCE_REMOVED";

   /**
    * An error has been added.
    */
   public static final Object ERROR_ADDED = "ERROR_ADDED";

   /**
    * An error has been removed.
    */
   public static final Object ERROR_REMOVED = "ERROR_REMOVED";

   /**
    * All errors have been removed from this source.
    */
   public static final Object ERRORS_CLEARED = "ERRORS_CLEARED";

   /**
    * Creates a new error source update message.
    *
    * @param errorSource      The message source
    * @param what        What changed
    * @param error       The error. Null unless what is ERROR_ADDED or
    *                    ERROR_REMOVED
    */
   public ErrorSourceUpdate(ErrorSource errorSource, Object what, ErrorSource.Error error) {
      super(null);
      if (what == null || errorSource == null)
         throw new NullPointerException("What and error source must be non-null");

      this.what = what;
      this.errorSource = errorSource;
      this.error = error;
   }

   /**
    * Returns what changed.
    */
   public Object getWhat() {
      return what;
   }

   /**
    * Returns the error source.
    */
   public ErrorSource getErrorSource() {
      return errorSource;
   }

   /**
    * Returns the error involved. Null if what is ERRORS_CLEARED.
    */
   public ErrorSource.Error getError() {
      return error;
   }

   public String paramString() {
      return super.paramString() + ",what=" + what
            + ",errorSource=" + errorSource
            + ",error=" + error;
   }

   private Object what;
   private ErrorSource errorSource;
   private ErrorSource.Error error;
}
