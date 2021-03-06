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

package piranha.Map;

import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * @author ineaugh
 */
public class Map
{
  Cell grid[][] = null;
  
  public Cell GetCell(int x, int y) { return grid[x][y]; }
  public Cell GetCell(Point p) { return grid[p.x][p.y]; }
  
  public int GetWidth() { return grid.length; } 
  public int GetHeight() { return grid[0].length; }
  
  public void Initialize(Dimension dim)
  {
    Initialize(dim.width, dim.height);
  }
          
  public void Initialize(int width, int height)
  {
    grid = new Cell[width][height];
    for(int x = 0; x < width; ++x)    
      for(int y = 0; y < height; ++y)
        grid[x][y] = new Cell();
  }
  
  public boolean IsInitialized() { return grid != null; }
}
