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

import gnu.trove.list.array.TIntArrayList;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ineaugh
 */
public class PointSet extends PointSetBase implements Iterable<Point>
{
  public PointSet(int extent)
  {
    super(extent);
  }
  
  public void Add(int x, int y)
  {
    int idx = MakeLeaf(x, y, true);
    
    int child = CalcChild(x, y, 0);
    tree.setQuick(idx, tree.getQuick(idx) | (1 << child));
  }
  
  public boolean Get(int x, int y)
  {
    int idx = GetLeaf(x, y, 1);
    if(idx == -1)
      return false;
    
    int child = CalcChild(x, y, 0);
    return (tree.getQuick(idx) & (1 << child)) != 0;
  }

  class PointSetIterator extends Walker implements Iterator<Point>
  {
    @Override
    public boolean hasNext()
    {
      while(true)
      {
        if(!Ascend())
          return false;
        
        int child = NextChild();
        if(level == levels - 1)
        {
          if((tree.getQuick(indexes[level]) & (1 << child)) != 0)
          {
            Descend(child);            
            return true;
          }
        }
        else
        {
          int idx = ChildIdx(child);
          if(idx != 0)
          {
            Descend(child);            
            OpenNode(idx);
          }
        } 
      }
    }

    @Override
    public Point next()
    {
      return current;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
  
  @Override
  public Iterator<Point> iterator()
  {
    return new PointSetIterator();
  }
  
  void FindNearestNeighbor(int x, int y, Walker walker, Point result, int range)
  {
    while(walker.Ascend())
    {
      int child = walker.NextChild();
      
      if(walker.GetLevel() == levels - 1)
      {
        if((tree.getQuick(walker.GetNodeIdx()) & (1 << child)) != 0)
        {
          walker.Descend(child);
          int minPossibleDistance = walker.MinPossibleDistance(x, y);
          
          if(minPossibleDistance >= range || minPossibleDistance == 0)
            continue;
          
          range = minPossibleDistance;
          result.setLocation(walker.GetNodePos());          
        }        
      }
      else
      {
        int idx = walker.ChildIdx(child);
        if(idx != 0)
        {
          walker.Descend(child);
          int minPossibleDistance = walker.MinPossibleDistance(x, y);

          if(minPossibleDistance < range)
            walker.OpenNode(idx);         
        }
      }
    }
  }
  
  public Point FindNearestNeighbor(int x, int y)
  {
    Point point = new Point();    
    FindNearestNeighbor(x, y, new Walker(), point, Integer.MAX_VALUE);
    return point;
  }
 
  public LineSegment FindNearestPair(Iterable<Point> otherLine)
  {
    int range = Integer.MAX_VALUE;
    LineSegment result = new LineSegment(new Point(), new Point());
    Walker walker = new Walker();
    
    for(Point other : otherLine)
    {
      walker.Reset();
      FindNearestNeighbor(other.x, other.y, walker, result.end, range);
      int newRange = GeometryTools.L1Distance(other, result.end);
      if(newRange < range)
      {
        range = newRange;
        result.begin.setLocation(other);
      }
    }
    
    return result;
  }
}
