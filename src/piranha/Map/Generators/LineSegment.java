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

package piranha.Map.Generators;

import java.awt.Point;
import java.util.Iterator;

/**
 *
 * @author ineaugh
 */
public class LineSegment implements Iterable<Point>
{
  public Point begin, end;

  public LineSegment()
  {
    begin = new Point();
    end = new Point();
  }
  
  public LineSegment(Point begin, Point end)
  {
    this.begin = begin;
    this.end = end;
  }

  @Override
  public Iterator<Point> iterator()
  {
    return new Bresenham4();
  }
  
  public class Bresenham4 implements Iterator<Point>
  {
    int dx = Math.abs(begin.x - end.x), dy = Math.abs(begin.y - end.y);
    int ix = begin.x < end.x ? 1 : -1, iy = begin.y < end.y ? 1 : -1;
    int e = 0, i = 0;
    Point current = new Point(begin);
    
    @Override
    public boolean hasNext()
    {
      return i < dx + dy - 1;
    }

    @Override
    public Point next()
    {
      int e1 = e + dy, e2 = e - dx;
      if(Math.abs(e1) < Math.abs(e2))
      {
        current.x += ix;
        e = e1;
      }
      else
      {
        current.y += iy;
        e = e2;
      }
      
      ++i;      
      return current;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
  
  public int L1Length()
  {
    return GeometryTools.L1Distance(begin, end);
  }
}
