/*
 * PositionManager.java - Manages positions
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2003 Slava Pestov
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

package org.gjt.sp.jedit.buffer;

//{{{ Imports
import javax.swing.text.Position;
import java.util.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * A class internal to jEdit's document model. You should not use it
 * directly.
 *
 * @author Slava Pestov
 * @version $Id: PositionManager.java 4882 2003-09-08 04:44:10Z spestov $
 * @since jEdit 4.2pre3
 */
public class PositionManager
{
	//{{{ createPosition() method
	public synchronized Position createPosition(int offset)
	{
		PosBottomHalf bh = new PosBottomHalf(offset);
		PosBottomHalf existing = (PosBottomHalf)positions.get(bh);
		if(existing == null)
		{
			positions.put(bh,bh);
			existing = bh;
		}

		return new PosTopHalf(existing);
	} //}}}

	//{{{ contentInserted() method
	public synchronized void contentInserted(int offset, int length)
	{
		if(positions.size() == 0)
			return;

		/* get all positions from offset to the end, inclusive */
		Iterator iter = positions.tailMap(new PosBottomHalf(offset))
			.keySet().iterator();

		iteration = true;
		while(iter.hasNext())
		{
			PosBottomHalf bh = (PosBottomHalf)iter.next();
			bh.offset += length;
		}
		iteration = false;
	} //}}}

	//{{{ contentRemoved() method
	public synchronized void contentRemoved(int offset, int length)
	{
		if(positions.size() == 0)
			return;

		/* get all positions from offset to the end, inclusive */
		Iterator iter = positions.tailMap(new PosBottomHalf(offset))
			.keySet().iterator();

		iteration = true;
		while(iter.hasNext())
		{
			PosBottomHalf bh = (PosBottomHalf)iter.next();
			if(bh.offset <= offset + length)
				bh.offset = offset;
			else
				bh.offset -= length;
		}
		iteration = false;

	} //}}}

	boolean iteration;

	//{{{ Private members
	private SortedMap positions = new TreeMap();
	//}}}

	//{{{ Inner classes

	//{{{ PosTopHalf class
	class PosTopHalf implements Position
	{
		PosBottomHalf bh;

		//{{{ PosTopHalf constructor
		PosTopHalf(PosBottomHalf bh)
		{
			this.bh = bh;
			bh.ref();
		} //}}}

		//{{{ getOffset() method
		public int getOffset()
		{
			return bh.offset;
		} //}}}

		//{{{ finalize() method
		public void finalize()
		{
			synchronized(PositionManager.this)
			{
				bh.unref();
			}
		} //}}}
	} //}}}

	//{{{ PosBottomHalf class
	class PosBottomHalf implements Comparable
	{
		int offset;
		int ref;

		//{{{ PosBottomHalf constructor
		PosBottomHalf(int offset)
		{
			this.offset = offset;
		} //}}}

		//{{{ ref() method
		void ref()
		{
			ref++;
		} //}}}

		//{{{ unref() method
		void unref()
		{
			if(--ref == 0)
				positions.remove(this);
		} //}}}

		//{{{ equals() method
		public boolean equals(Object o)
		{
			if(!(o instanceof PosBottomHalf))
				return false;

			return ((PosBottomHalf)o).offset == offset;
		} //}}}

		//{{{ compareTo() method
		public int compareTo(Object o)
		{
			if(iteration)
				Log.log(Log.ERROR,this,"Consistency failure");
			return offset - ((PosBottomHalf)o).offset;
		} //}}}
	} //}}}

	//}}}
}
