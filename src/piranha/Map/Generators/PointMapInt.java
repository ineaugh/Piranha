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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ineaugh
 */
public class PointMapInt extends PointSetBase implements Iterable<PointMapInt.Entry>
{
  public PointMapInt(int extent)
  {
    super(extent);
  }
  
  public void Add(int x, int y, int value)
  {
    int idx = MakeLeaf(x, y, false);
    int child = CalcChild(x, y, 0);
    tree.setQuick(idx + child, value + 1);
  }
  
  public int Get(int x, int y)
  {
    return GetLeaf(x, y, 0) - 1;
  }

  @Override
  public Iterator<Entry> iterator()
  {
    return new PointMapIntIterator();
  }
  
  public class Entry
  {
    public Point point;
    public int value;
  }
  
  class PointMapIntIterator extends Walker implements Iterator<Entry>
  {
    Entry entry = new Entry();
    
    public PointMapIntIterator()
    {
      entry.point = current;
    }
    
    @Override
    public boolean hasNext()
    {
      while(true)
      {
        if(!Ascend())
          return false;
        
        int child = NextChild();
        int idx = ChildIdx(child);
        if(idx == 0)
          continue;

        Descend(child);            
        
        if(level == levels - 1)
        {
          entry.value = idx - 1;
          return true;
        }
        else
          OpenNode(idx);
      }    
    }

    @Override
    public Entry next()
    {
      return entry;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
  
  public class EntryConnection
  {
    public LineSegment connection;
    public int beginValue, endValue;
  }  
  
  void FindNearestNeighbor(int x, int y, Walker walker, Entry entry, int range, int ignore)
  {
    while(walker.Ascend())
    {
      int child = walker.NextChild();
      int idx = walker.ChildIdx(child);
      if(idx != 0)
      {
        walker.Descend(child);
        int minPossibleDistance = walker.MinPossibleDistance(x, y);
        
        if(minPossibleDistance >= range)
          continue;
        
        if(walker.GetLevel() == levels - 1)
        {
          if((walker.GetNodePos().x != x || walker.GetNodePos().y != y) && idx != ignore)
          {
            range = minPossibleDistance;
            entry.value = idx - 1;
            entry.point.setLocation(walker.GetNodePos());
          }
        }
        else
          walker.OpenNode(idx);
      }
    }
  }  
  
  public List<EntryConnection> FindClosestConnections()
  {
    class Connection
    {
      LineSegment direction = new LineSegment();
      int otherValue, distance = Integer.MAX_VALUE;
    }
    
    ArrayList<Connection> connections = new ArrayList<>();
    
    Walker neighborWalker = new Walker();
    Entry nearest = new Entry();
    nearest.point = new Point();    
    
    for(Entry e : this)
    {
      while(connections.size() < e.value + 1)
        connections.add(new Connection());
      
      neighborWalker.Reset();
      nearest.value = -1;
      Connection c = connections.get(e.value);
      FindNearestNeighbor(e.point.x, e.point.y, neighborWalker, nearest, c.distance, e.value + 1);
      if(nearest.value != -1)
      {
        c.direction.begin.setLocation(e.point);
        c.direction.end.setLocation(nearest.point);
        c.otherValue = nearest.value;
        c.distance = c.direction.L1Length();
        
        while(connections.size() < nearest.value + 1)
          connections.add(new Connection());        
        
        Connection other = connections.get(nearest.value);
        if(other.distance > c.distance)
        {
          other.direction.begin.setLocation(nearest.point);
          other.direction.end.setLocation(e.point);
          other.otherValue = e.value;
          other.distance = c.distance;
        }
      }
    }
    
    ArrayList<EntryConnection> result = new ArrayList<>();
    for(int i = 0; i < connections.size(); ++i)
    {
      Connection c = connections.get(i);
      EntryConnection ec = new EntryConnection();
      ec.connection = c.direction;
      ec.beginValue = i;
      ec.endValue = c.otherValue;
      result.add(ec);
    }
    
    return result;
  }
}
