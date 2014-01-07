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

import java.awt.Color;
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
import java.lang.reflect.Constructor;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import piranha.Map.Cell;
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
  int keyModifiers = 0;

  MapRender render;
  Movable character;
  Circles map;
  
  Maze maze;
  
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
    maze = map.Initialize(new Dimension(50, 100), rand);
    
    for(Point p : map.GetSpecialPoints())
      map.GetCell(p).ChangeType(StandardCellTypes.Mark);
    
    character = new Movable(characterType, map);
    List<Point> freePlaces = new ArrayList<>();
    for (Point pt : maze.GetFreeCells())
      freePlaces.add(new Point(pt));
      
    character.Teleport(freePlaces.get(rand.nextInt(freePlaces.size())));
    
    float distances[][] = new float[maze.GetWidth()][maze.GetHeight()];
    maze.FillDistances(character.GetCoordinate(), distances);
    
    Point farSpecial = map.GetSpecialPoints().get(0);
    for(Point p : map.GetSpecialPoints())
      if(distances[p.x][p.y] > distances[farSpecial.x][farSpecial.y])
        farSpecial = p;
    
    map.GetCell(farSpecial).ChangeType(StandardCellTypes.PassageUp);
    
    render = new MapRender(map, character, terminal, new Rectangle(terminal.getGridWidth(), terminal.getGridHeight() - 1));
    render.ShiftX(character.GetCoordinate().x - terminal.getGridWidth() / 2);
    render.ShiftY(character.GetCoordinate().y - terminal.getGridHeight() / 2);
    
    UpdateScreen();
    
    frame.addKeyListener(this);
    terminal.addMouseListener(this);
    terminal.addMouseMotionListener(this);    
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
  
  static int directionKeys[][] = {
    {KeyEvent.VK_W, KeyEvent.VK_NUMPAD8, KeyEvent.VK_UP},
    {KeyEvent.VK_E, KeyEvent.VK_NUMPAD9}, 
    {KeyEvent.VK_D, KeyEvent.VK_NUMPAD6, KeyEvent.VK_RIGHT},
    {KeyEvent.VK_C, KeyEvent.VK_NUMPAD3},
    {KeyEvent.VK_X, KeyEvent.VK_S, KeyEvent.VK_NUMPAD2, KeyEvent.VK_DOWN},
    {KeyEvent.VK_Z, KeyEvent.VK_NUMPAD1},
    {KeyEvent.VK_A, KeyEvent.VK_NUMPAD4, KeyEvent.VK_LEFT},
    {KeyEvent.VK_Q, KeyEvent.VK_NUMPAD7}};  

  @Override
  public void keyPressed(KeyEvent ke)
  {
    keyModifiers = ke.getModifiers();
    //System.err.println("Key pressed");
    int code = ke.getKeyCode();
    if(code == KeyEvent.VK_ESCAPE)
      frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
   
    Direction dir = Direction.NONE;
    for(int i = 0; i < Utils.ClockwiseDirs.length; ++i)
      for(int j = 0; j < directionKeys[i].length; ++j)
        if(code == directionKeys[i][j])
          dir = Utils.ClockwiseDirs[i];
    
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
    
    if(code == KeyEvent.VK_ENTER)
      if(map.GetCell(pos).GetType() == StandardCellTypes.PassageUp)
      {      
        map = new Circles();
        maze = map.Initialize(new Dimension(50, 100), rand, pos);

        for(Point p : map.GetSpecialPoints())
          map.GetCell(p).ChangeType(StandardCellTypes.Mark);
        
        map.GetCell(pos).ChangeType(StandardCellTypes.PassageDown);

        float distances[][] = new float[maze.GetWidth()][maze.GetHeight()];
        maze.FillDistances(pos, distances);

        Point farSpecial = map.GetSpecialPoints().get(0);
        for(Point p : map.GetSpecialPoints())
          if(distances[p.x][p.y] > distances[farSpecial.x][farSpecial.y])
            farSpecial = p;

        map.GetCell(farSpecial).ChangeType(StandardCellTypes.PassageUp);  
        
        render.SwitchMap(map);
        character.MoveToMap(map, pos);
      }
    
    UpdateScreen();    
  }    

  private void UpdateScreen()
  {
    long start = System.nanoTime();
    render.Render((keyModifiers & KeyEvent.CTRL_MASK) != 0);
    String status = String.format("At %d:%d.", character.GetCoordinate().x, character.GetCoordinate().y);
    if(map.GetCell(character.GetCoordinate()).GetType() == StandardCellTypes.PassageDown)
      status = status.concat(" [Enter]: descend.");
    else if(map.GetCell(character.GetCoordinate()).GetType() == StandardCellTypes.PassageUp)
      status = status.concat(" [Enter]: ascend.");    
    
    while(status.length() < terminal.getGridWidth())
      status = status.concat(" ");
    
    terminal.placeHorizontalString(0, terminal.getGridHeight() - 1, status);
    terminal.refresh();

    //frame.repaint();
    System.out.format("Update time: %d ms\n", (System.nanoTime() - start) / 1000000);    
  }

  @Override
  public void keyReleased(KeyEvent ke)
  {
    keyModifiers = ke.getModifiers(); 
    UpdateScreen();
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
