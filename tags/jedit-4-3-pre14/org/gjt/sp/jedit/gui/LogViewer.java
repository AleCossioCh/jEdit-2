/*
 * LogViewer.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2004 Slava Pestov
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

package org.gjt.sp.jedit.gui;

//{{{ Imports
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.Log;
//}}}

/**
 * @version $Id: LogViewer.java 12504 2008-04-22 23:12:43Z ezust $
 */
public class LogViewer extends JPanel implements DefaultFocusComponent,
	EBComponent
{
	//{{{ LogViewer constructor
	public LogViewer()
	{
		super(new BorderLayout());

		JPanel caption = new JPanel();
		caption.setLayout(new BoxLayout(caption,BoxLayout.X_AXIS));
		caption.setBorder(new EmptyBorder(6,6,6,6));

		String settingsDirectory = jEdit.getSettingsDirectory();
		if(settingsDirectory != null)
		{
			String[] args = { MiscUtilities.constructPath(
				settingsDirectory, "activity.log") };
			JLabel label = new JLabel(jEdit.getProperty(
				"log-viewer.caption",args));
			caption.add(label);
		}

		caption.add(Box.createHorizontalGlue());

		tailIsOn = jEdit.getBooleanProperty("log-viewer.tail", false);
		tail = new JCheckBox(
			jEdit.getProperty("log-viewer.tail.label"),tailIsOn);
		tail.addActionListener(new ActionHandler());


		filter = new JTextField();
		filter.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e) 
			{
				setFilter();
			}
			
			public void insertUpdate(DocumentEvent e) 
			{
				setFilter();
			}
			
			public void removeUpdate(DocumentEvent e) 
			{
				setFilter();
			}
		});
		caption.add(filter);
		caption.add(tail);

		caption.add(Box.createHorizontalStrut(12));

		copy = new JButton(jEdit.getProperty("log-viewer.copy"));
		copy.addActionListener(new ActionHandler());
		caption.add(copy);

		ListModel model = Log.getLogListModel();
		listModel = new FilteredListModel(model) 
		{

			@Override
			protected void restoreSelection(Set selectedIndices) 
			{
				super.restoreSelection(selectedIndices);
			}
			
			@Override
			protected Set saveSelection() 
			{
				return super.saveSelection();
			}

			@Override
			public String prepareFilter(String filter) 
			{
				return filter.toLowerCase();
			}
			
			@Override
			public boolean passFilter(int row, String filter) 
			{
				return delegated.getElementAt(row).toString().toLowerCase().contains(filter);
			}
			
			
		};
		model.addListDataListener(new ListHandler());
		list = new LogList(listModel);
		listModel.setList(list);
		add(BorderLayout.NORTH,caption);
		JScrollPane scroller = new JScrollPane(list);
		Dimension dim = scroller.getPreferredSize();
		dim.width = Math.min(600,dim.width);
		scroller.setPreferredSize(dim);
		add(BorderLayout.CENTER,scroller);

		propertiesChanged();
	} //}}}

	//{{{ setBounds() method
	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		scrollLaterIfRequired();
	} //}}}

	//{{{ handleMessage() method
	public void handleMessage(EBMessage msg)
	{
		if(msg instanceof PropertiesChanged)
			propertiesChanged();
	} //}}}

	//{{{ addNotify() method
	@Override
	public void addNotify()
	{
		super.addNotify();
		if(tailIsOn)
			scrollToTail();

		EditBus.addToBus(this);
	} //}}}

	//{{{ removeNotify() method
	@Override
	public void removeNotify()
	{
		super.removeNotify();

		EditBus.removeFromBus(this);
	} //}}}

	//{{{ focusOnDefaultComponent() method
	public void focusOnDefaultComponent()
	{
		list.requestFocus();
	} //}}}

	//{{{ Private members
	private final FilteredListModel listModel;
	private final JList list;
	private final JButton copy;
	private final JCheckBox tail;
	private final JTextField filter;
	private boolean tailIsOn;

	//{{{ setFilter() method
	private void setFilter()
	{
		listModel.setFilter(filter.getText());
		scrollLaterIfRequired();
	} //}}}

	//{{{ propertiesChanged() method
	private void propertiesChanged()
	{
		list.setFont(jEdit.getFontProperty("view.font"));
		list.setFixedCellHeight(list.getFontMetrics(list.getFont())
			.getHeight());
	} //}}}

	//{{{ scrollToTail() method
	/** Scroll to the tail of the logs. */
	private void scrollToTail()
	{
		int index = list.getModel().getSize();
		if(index != 0)
			list.ensureIndexIsVisible(index - 1);
	} //}}}

	//{{{ scrollLaterIfRequired() method
	private void scrollLaterIfRequired()
	{
		if (tailIsOn)
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					scrollToTail();
				}
			});
	} //}}}

	//}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Object src = e.getSource();
			if(src == tail)
			{
				tailIsOn = !tailIsOn;
				jEdit.setBooleanProperty("log-viewer.tail",tailIsOn);
				if(tailIsOn)
				{
					scrollToTail();
				}
			}
			else if(src == copy)
			{
				StringBuilder buf = new StringBuilder();
				Object[] selected = list.getSelectedValues();
				if(selected != null && selected.length != 0)
				{
					for(int i = 0; i < selected.length; i++)
					{
						buf.append(selected[i]);
						buf.append('\n');
					}
				}
				else
				{
					ListModel model = list.getModel();
					for(int i = 0; i < model.getSize(); i++)
					{
						buf.append(model.getElementAt(i));
						buf.append('\n');
					}
				}
				Registers.setRegister('$',buf.toString());
			}
		}
	} //}}}

	//{{{ ListHandler class
	class ListHandler implements ListDataListener
	{
		public void intervalAdded(ListDataEvent e)
		{
			contentsChanged(e);
		}

		public void intervalRemoved(ListDataEvent e)
		{
			contentsChanged(e);
		}

		public void contentsChanged(ListDataEvent e)
		{
			scrollLaterIfRequired();
		}
	} //}}}

	//{{{ LogList class
	class LogList extends JList
	{
		LogList(ListModel model)
		{
			super(model);
			setVisibleRowCount(24);
			getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			setAutoscrolls(true);
		}

		@Override
		public void processMouseEvent(MouseEvent evt)
		{
			if(evt.getID() == MouseEvent.MOUSE_PRESSED)
			{
				startIndex = list.locationToIndex(
					evt.getPoint());
			}
			super.processMouseEvent(evt);
		}

		@Override
		public void processMouseMotionEvent(MouseEvent evt)
		{
			if(evt.getID() == MouseEvent.MOUSE_DRAGGED)
			{
				int row = list.locationToIndex(evt.getPoint());
				if(row != -1)
				{
					if(startIndex == -1)
					{
						list.setSelectionInterval(row,row);
						startIndex = row;
					}
					else
						list.setSelectionInterval(startIndex,row);
					list.ensureIndexIsVisible(row);
					evt.consume();
				}
			}
			else
				super.processMouseMotionEvent(evt);
		}

		private int startIndex;
	} //}}}
}
