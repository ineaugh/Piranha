/*
 * The MIT License
 *
 * Copyright 2013 ineaugh.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package piranha;

import piranha.Map.Map;
import piranha.Map.CellType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import piranha.Map.Generators.LineSegment;
import piranha.Map.Generators.Maze;
import piranha.Map.Generators.Passage;
import piranha.Map.Generators.PointMap;
import piranha.Map.Generators.PointMapInt;
import piranha.Map.Generators.PointSet;
import piranha.Map.Generators.Room;
import piranha.Map.Generators.Simple;
import piranha.Map.Generators.StandardCellTypes;
import piranha.Map.Generators.Utils;
import piranha.Map.Movable;
import piranha.Map.Places.Circles;
import squidpony.squidgrid.gui.SGPane;
import squidpony.squidgrid.gui.swing.SwingPane;
import squidpony.squidgrid.util.Direction;


/**
 *
 * @author ineaugh
 */
public class Piranha implements KeyListener, MouseListener, MouseMotionListener
{
  SwingPane terminal = new SwingPane(110, 50, new Font("Courier New", Font.PLAIN, 18));
  JFrame frame = new JFrame("Tower of Piranha");
  Random rand = new Random();
  Point dragFrom = new Point();

  MapRender render;
  Movable character;
  Circles map;
  
  public Piranha()
  {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
    
    frame.add(terminal);
    int w = terminal.getPreferredSize().width;
    int h = terminal.getPreferredSize().height;

    frame.setResizable(false);    
    frame.getContentPane().setPreferredSize(new Dimension(w, h));
    frame.pack();
    frame.setLocationByPlatform(true);
    frame.setVisible(true);
  }
  
  public void Initialize()
  {   
    CellType characterType = new CellType('@', true);   
    map = new Circles();
    Maze maze = map.Initialize(rand);
    
    character = new Movable(characterType, map);
    List<Point> freePlaces = new ArrayList<>();
    for (Point pt : maze.GetFreeCells())
      freePlaces.add(new Point(pt));
      
    character.Teleport(freePlaces.get(rand.nextInt(freePlaces.size())));
    
    for(Point p : map.GetSpecialPoints())
      map.GetCell(p).ChangeType(StandardCellTypes.Mark);
    
    render = new MapRender(map, character, terminal, new Rectangle(terminal.getGridWidth(), terminal.getGridHeight()));
    render.ShiftX(character.GetCoordinate().x - terminal.getGridWidth() / 2);
    render.ShiftY(character.GetCoordinate().y - terminal.getGridHeight() / 2);
    
    Draw();
    
    frame.addKeyListener(this);
    terminal.addMouseListener(this);
    terminal.addMouseMotionListener(this);    
  }
  
  void Draw()
  {
    render.Render();
    terminal.refresh();
  }
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) 
  {
    Piranha game = new Piranha();
    game.Initialize();
  }

  @Override
  public void keyTyped(KeyEvent ke)
  {
  }

  @Override
  public void keyPressed(KeyEvent ke)
  {
    //System.err.println("Key pressed");
    int code = ke.getKeyCode();
    if(code == KeyEvent.VK_ESCAPE)
      frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
   
    Direction dir = Direction.NONE;
    
    if(code == KeyEvent.VK_W || code == KeyEvent.VK_NUMPAD8 || code == KeyEvent.VK_UP)
      dir = Direction.UP;
    
    if(code == KeyEvent.VK_E || code == KeyEvent.VK_NUMPAD9)
      dir = Direction.UP_RIGHT;
    
    if(code == KeyEvent.VK_D || code == KeyEvent.VK_NUMPAD6 || code == KeyEvent.VK_RIGHT)
      dir = Direction.RIGHT; 
    
    if(code == KeyEvent.VK_C || code == KeyEvent.VK_NUMPAD3)
      dir = Direction.DOWN_RIGHT;    
    
    if(code == KeyEvent.VK_X || code == KeyEvent.VK_S || code == KeyEvent.VK_NUMPAD2 || code == KeyEvent.VK_DOWN)
      dir = Direction.DOWN; 
    
    if(code == KeyEvent.VK_Z || code == KeyEvent.VK_NUMPAD1)
      dir = Direction.DOWN_LEFT; 
    
    if(code == KeyEvent.VK_A || code == KeyEvent.VK_NUMPAD4 || code == KeyEvent.VK_LEFT)
      dir = Direction.LEFT; 
    
    if(code == KeyEvent.VK_Q || code == KeyEvent.VK_NUMPAD7)
      dir = Direction.UP_LEFT;    
    
    Point pos = character.GetCoordinate();
    
    if(map.GetCell(pos.x + dir.deltaX, pos.y + dir.deltaY).IsBlocked())
    {
      Direction clockwise = dir.clockwise(), counterClockwise = dir.counterClockwise();
      boolean clockwiseFree = !map.GetCell(pos.x + clockwise.deltaX, pos.y + clockwise.deltaY).IsBlocked();
      boolean counterClockwiseFree = !map.GetCell(pos.x + counterClockwise.deltaX, pos.y + counterClockwise.deltaY).IsBlocked();
      if(clockwiseFree && !counterClockwiseFree)
        dir = clockwise;
      else if(counterClockwiseFree && ! clockwiseFree)
        dir = counterClockwise;
    }
    
    if(!dir.equals(Direction.NONE) && !map.GetCell(pos.x + dir.deltaX, pos.y + dir.deltaY).IsBlocked())
    {
      Rectangle view = render.GetView();
      int xBorder = view.width / 4;
      if(dir.deltaX > 0 && pos.x >= view.x + view.width - xBorder / 2 || pos.x >= view.x + view.width)
        render.ShiftX(character.GetCoordinate().x - view.x - view.width + xBorder);

      if(dir.deltaX < 0 && pos.x <= view.x + xBorder / 2 || pos.x < view.x)
        render.ShiftX(pos.x - view.x - xBorder);

      int yBorder = view.height / 4;
      if(dir.deltaY > 0 && pos.y >= view.y + view.height - yBorder / 2 || pos.y >= view.y + view.height)
        render.ShiftY(pos.y - view.y - view.height + yBorder);
      if(dir.deltaY < 0 && pos.y <= view.y + yBorder / 2 || pos.y < view.y)
        render.ShiftY(pos.y - view.y - yBorder);

      character.Shift(dir);
    }    
    UpdateScreen();    
  }    

  private void UpdateScreen()
  {
    long start = System.nanoTime();
    render.Render();
    terminal.refresh();
    //frame.repaint();
    System.out.format("Update time: %d ms\n", (System.nanoTime() - start) / 1000000);    
  }

  @Override
  public void keyReleased(KeyEvent ke)
  {
  }

  @Override
  public void mouseClicked(MouseEvent me)
  {
  }

  @Override
  public void mousePressed(MouseEvent me)
  {
    //System.err.println("pressed");
    if(me.getButton() == MouseEvent.BUTTON3)
      dragFrom.setLocation(me.getPoint());
    else
      dragFrom.setLocation(-1, -1);
  }

  @Override
  public void mouseReleased(MouseEvent me)
  {
  }

  @Override
  public void mouseEntered(MouseEvent me)
  {
  }

  @Override
  public void mouseExited(MouseEvent me)
  {
  }

  @Override
  public void mouseDragged(MouseEvent me)
  {
    if(dragFrom.x != -1)
    {
      render.ShiftX(dragFrom.x / terminal.getCellWidth() - me.getX() / terminal.getCellWidth());
      render.ShiftY(dragFrom.y / terminal.getCellHeight() - me.getY() / terminal.getCellHeight());
      //System.err.println(String.format("Dragging %dx%d", dragFrom.x, dragFrom.y));
      dragFrom.setLocation(me.getPoint()); 
      
      if(Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() == null)
        UpdateScreen();
    }
  }

  @Override
  public void mouseMoved(MouseEvent me)
  {
  }
}
