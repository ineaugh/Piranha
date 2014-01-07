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

import java.awt.Point;
import squidpony.squidgrid.util.Direction;

/**
 *
 * @author ineaugh
 */
public class Movable
{
  Point coordinate;
  CellType type;
  Map map;

  public Movable(CellType type, Map map)
  {
    this.coordinate = null;
    this.type = type;
    this.map = map;
  }
  
  public Point GetCoordinate()
  {
    return coordinate;
  }

  public void Teleport(Point coordinate)
  {
    if (this.coordinate != null)
    {
      map.GetCell(this.coordinate).movables.remove(this);
      this.coordinate.setLocation(coordinate);
    }
    else
      this.coordinate = new Point(coordinate);
    
    map.GetCell(coordinate).movables.add(this);
  }
  
  public void Shift(Direction direction)
  {
    map.GetCell(this.coordinate).movables.remove(this);  
    coordinate.translate(direction.deltaX, direction.deltaY);
    map.GetCell(coordinate).movables.add(this);
  }

  public CellType GetType()
  {
    return type;
  }

  public void MoveToMap(Map map, Point newPos)
  {
    map.GetCell(coordinate).movables.remove(this);     
    this.map = map;
    coordinate.setLocation(newPos);
    map.GetCell(coordinate).movables.add(this);    
  }
}
