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
 * @param <ValueType> Type of values for this map
 */
public class PointMap<ValueType> extends PointSetBase implements Iterable<PointMap<ValueType>.Entry>
{
  ArrayList<ValueType> values = new ArrayList<>();
  
  public PointMap(int extent)
  {
    super(extent);
    values.add(null);
  }  
  
  public void Add(int x, int y, ValueType value)
  {
    int idx = MakeLeaf(x, y, false);
    int child = CalcChild(x, y, 0);
    int pos = tree.getQuick(idx + child);
    if(pos == 0)
    {
      tree.setQuick(idx + child, values.size());
      values.add(value);
    }
    else
      values.set(pos, value);
  }
  
  public ValueType Get(int x, int y)
  {
    int idx = GetLeaf(x, y, 0);
    return idx == -1 ? null : values.get(idx);
  }

  public Entry FindNearestNeighbor(int x, int y)
  {
    Entry entry = new Entry();
    entry.point = new Point();    
    FindNearestNeighbor(x, y, new Walker(), entry);
    return entry;
  }
  
  void FindNearestNeighbor(int x, int y, Walker walker, Entry entry)
  {
    int range = Integer.MAX_VALUE;
    
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
          if(walker.GetNodePos().x != x || walker.GetNodePos().y != y)
          {
            range = minPossibleDistance;
            entry.value = values.get(idx);
            entry.point.setLocation(walker.GetNodePos());
          }
        }
        else
          walker.OpenNode(idx);
      }
    }
  }
  
  public class EntryPair
  {
    public ValueType first, second;
    public int L1Distance;
  }
  
  public List<EntryPair> FindPairwiseNearestValues()
  {
    ArrayList<EntryPair> results = new ArrayList<>();
    Walker neighborWalker = new Walker();
    Entry nearest = new Entry();
    nearest.point = new Point();    
    for(Entry e : this)
    {
      neighborWalker.Reset();
      FindNearestNeighbor(e.point.x, e.point.y, neighborWalker, nearest);
      EntryPair res = new EntryPair();
      res.first = e.value;
      res.second = nearest.value;
      res.L1Distance = GeometryTools.L1Distance(nearest.point, e.point);
      results.add(res);
    }
    
    return results;
  }
  
  public class Entry
  {
    public Point point;
    public ValueType value;
  }
  
  class PointMapIterator extends Walker implements Iterator<Entry>
  {
    Entry entry = new Entry();
    
    public PointMapIterator()
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
          entry.value = values.get(idx);
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
  
  @Override
  public Iterator<Entry> iterator()
  {
    return new PointMapIterator();
  }  
}
