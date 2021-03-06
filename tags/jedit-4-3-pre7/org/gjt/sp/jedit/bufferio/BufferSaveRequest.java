/*
 * BufferSaveRequest.java - I/O request
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2005 Slava Pestov
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

package org.gjt.sp.jedit.bufferio;

//{{{ Imports
import java.io.*;
import java.util.zip.*;

import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.*;
//}}}

/**
 * A buffer save request.
 * @author Slava Pestov
 * @version $Id: BufferSaveRequest.java 7129 2006-09-25 20:05:57Z kpouer $
 */
public class BufferSaveRequest extends BufferIORequest
{
	//{{{ BufferSaveRequest constructor
	/**
	 * Creates a new buffer I/O request.
	 * @param view The view
	 * @param buffer The buffer
	 * @param session The VFS session
	 * @param vfs The VFS
	 * @param path The path
	 */
	public BufferSaveRequest(View view, Buffer buffer,
		Object session, VFS vfs, String path)
	{
		super(view,buffer,session,vfs,path);
	} //}}}

	//{{{ run() method
	public void run()
	{
		OutputStream out = null;

		boolean vfsRenameCap = (vfs.getCapabilities() &
			VFS.RENAME_CAP) != 0;

		boolean overwriteReadOnly = buffer.getBooleanProperty("overwriteReadonly");

		/* if the VFS supports renaming files, we first
		 * save to #<filename>#save#, then rename that
		 * to <filename>, so that if the save fails,
		 * data will not be lost.
		 *
		 * as of 4.1pre7 we now call vfs.getTwoStageSaveName()
		 * instead of constructing the path directly
		 * since some VFS's might not allow # in filenames.
		 */

		boolean twoStageSave = overwriteReadOnly ||
			(vfsRenameCap &&
			jEdit.getBooleanProperty("twoStageSave"));

		try
		{
			String[] args = { vfs.getFileName(path) };
			setStatus(jEdit.getProperty("vfs.status.save",args));

			// the entire save operation can be aborted...
			setAbortable(true);

			path = vfs._canonPath(session,path,view);			
			if(!MiscUtilities.isURL(path))
				path = MiscUtilities.resolveSymlinks(path);


			// Only backup once per session
			if(buffer.getProperty(Buffer.BACKED_UP) == null
				|| jEdit.getBooleanProperty("backupEverySave"))
			{
				vfs._backup(session,path,view);
				buffer.setBooleanProperty(Buffer.BACKED_UP,true);
			}

			String savePath;
			if(twoStageSave)
			{
				savePath = vfs.getTwoStageSaveName(path);
				if (savePath == null)
				{
					twoStageSave = false;
					savePath = path;
				}
			}
			else
				savePath = path;

			out = vfs._createOutputStream(session,savePath,view);

			try
			{
				// this must be after the stream is created or
				// we deadlock with SSHTools.
				buffer.readLock();
				if(out != null)
				{
					// Can't use buffer.getName() here because
					// it is not changed until the save is
					// complete
					if(path.endsWith(".gz"))
						buffer.setBooleanProperty(Buffer.GZIPPED,true);
					else if (buffer.getName().endsWith(".gz"))
					{
						// The path do not ends with gz.
						// The buffer name was .gz.
						// So it means it's blabla.txt.gz -> blabla.txt, I remove
						// the gz property
						buffer.setBooleanProperty(Buffer.GZIPPED, false);
					}

					if(buffer.getBooleanProperty(Buffer.GZIPPED))
						out = new GZIPOutputStream(out);

					write(buffer,out);

					if(twoStageSave)
					{
						if(!vfs._rename(session,savePath,path,view))
							throw new IOException("Rename failed: " + savePath);
					}

				}
				else
					buffer.setBooleanProperty(ERROR_OCCURRED,true);

				if(!twoStageSave)
					VFSManager.sendVFSUpdate(vfs,path,true);
			}
			finally
			{
				buffer.readUnlock();
			}
		}
		catch(IOException io)
		{
			Log.log(Log.ERROR,this,io);
			String[] pp = { io.toString() };
			VFSManager.error(view,path,"ioerror.write-error",pp);

			buffer.setBooleanProperty(ERROR_OCCURRED,true);
		}
		catch(WorkThread.Abort a)
		{
			IOUtilities.closeQuietly(out);
			buffer.setBooleanProperty(ERROR_OCCURRED,true);
		}
		finally
		{
			try
			{
				vfs._saveComplete(session,buffer,path,view);
				if( twoStageSave )
				{
					vfs._finishTwoStageSave(session,buffer,path,view);
				}
				// clean up left-over markers file
				if(!jEdit.getBooleanProperty("persistentMarkers"))
					vfs._delete(session,buffer.getMarkersPath(vfs),view);
				vfs._endVFSSession(session,view);
			}
			catch(IOException io)
			{
				Log.log(Log.ERROR,this,io);
				String[] pp = { io.toString() };
				VFSManager.error(view,path,"ioerror.write-error",pp);

				buffer.setBooleanProperty(ERROR_OCCURRED,true);
			}
			catch(WorkThread.Abort a)
			{
				buffer.setBooleanProperty(ERROR_OCCURRED,true);
			}
		}
	} //}}}
}
