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
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;
import piranha.Map.Cell;
import piranha.Map.Generators.StandardCellTypes;
import piranha.Map.Movable;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.fov.EliasFOV;
import squidpony.squidgrid.fov.FOVSolver;
import squidpony.squidgrid.fov.RayCastingFOV;
import squidpony.squidgrid.fov.RippleFOV;
import squidpony.squidgrid.fov.ShadowFOV;
import squidpony.squidgrid.fov.SpreadFOV;
import squidpony.squidgrid.fov.TranslucenceWrapperFOV;
import squidpony.squidgrid.gui.SGPane;

/**
 *
 * @author ineaugh
 */
public class MapRender
{
  Map map;
  SGPane terminal;
  Rectangle view;
  float[][] obstacles;
  Movable character;
  FOVSolver fovSolver;
  Random flickerRand = new Random();
  
  public MapRender(Map map, Movable character, SGPane terminal, Rectangle view)
  {
    this.map = map;
    this.terminal = terminal;
    this.view = new Rectangle(view);
    this.character = character;
    obstacles = new float[map.GetWidth()][map.GetHeight()];
    fovSolver = new ShadowFOV();
  }
  
  public void Render(boolean seenAll)
  {
    for(int x = 0; x < map.GetWidth(); ++x)
      for(int y = 0; y < map.GetHeight(); ++y)
        if(map.GetCell(x, y).GetType() == StandardCellTypes.Wall)
          obstacles[x][y] = 1;
        else
          obstacles[x][y] = 0;
    
    float visibility[][] = fovSolver.calculateFOV(obstacles, character.GetCoordinate().x, character.GetCoordinate().y, 40);
    visibility[character.GetCoordinate().x][character.GetCoordinate().y] = 1;
    
    for(int x = 0; x < view.width; ++x)    
      for(int y = 0; y < view.height; ++y)
      {
        int vx = view.x + x, vy = view.y + y;
        if(vx < map.GetWidth() && vy < map.GetHeight() && y < terminal.getGridHeight() && x < terminal.getGridWidth())
        {
          Cell c = map.GetCell(vx, vy);
          CellType t = c.GetType();
          if(!c.GetMovables().isEmpty())
            t = c.GetMovables().iterator().next().GetType();

          float vis = visibility[vx][vy];
          if(vis > 0)
            c.SetSeen(true);

          if(c.IsSeen() || seenAll)
            vis = Math.max(0.3f, vis);

          terminal.placeCharacter(x, y, t.GetSymbol(), 
                    Color.getHSBColor(0, 0, vis), 
                    Color.getHSBColor(0, 0, 0.2f * (1 + flickerRand.nextFloat() * 0.05f) * vis));
        }
      }
  }
  
  static float ClampColor(float c)
  {
    return Math.max(0, Math.min(1, c));
  }
  
  public Rectangle GetView() { return view; }
  
  public void Shift(Point shift)
  {
    ShiftX(shift.x);
    ShiftY(shift.y);
  }
  
  public void ShiftX(int shift)
  {
     view.x = Math.max(0, Math.min(map.GetWidth() - view.width, view.x + shift));
  }
  
  public void ShiftY(int shift)
  {
     view.y = Math.max(0, Math.min(map.GetHeight() - view.height, view.y + shift));
  }  

  public void SwitchMap(Map map)
  {
    this.map = map;
  }
}
