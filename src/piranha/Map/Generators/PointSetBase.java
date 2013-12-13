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

/**
 *
 * @author ineaugh
 */
public abstract class PointSetBase
{
  TIntArrayList tree = new TIntArrayList();
  int levels;

  public PointSetBase(int extent)
  {
    levels = (int)Math.ceil(Math.log(extent) / Math.log(2));
    for(int i = 0; i < 4; ++i)
      tree.add(0);
  }

  protected int MakeLeaf(int x, int y, boolean compact)
  {
    int idx = 0;
    for (int i = levels - 1; i >= 1; --i)
    {
      int child = ((x >> i) & 1) + (((y >> i) & 1) << 1);
      int newIdx = tree.getQuick(idx + child);
      if (newIdx == 0)
      {
        newIdx = tree.size();
        tree.setQuick(idx + child, newIdx);
        tree.add(0);
        if (i > 1 || !compact)
          for (int j = 0; j < 3; ++j)
            tree.add(0);
      }
      idx = newIdx;
    }
    
    return idx;
  }
  
  protected int CalcChild(int x, int y, int bit)
  {
    return ((x >> bit) & 1) + (((y >> bit) & 1) << 1);    
  }
  
  protected int GetLeaf(int x, int y, int onLevel)
  {
    int idx = 0;
    for(int i = levels - 1; i >= onLevel; --i)
    {
      int child = CalcChild(x, y, i);
      idx = tree.getQuick(idx + child);
      if(idx == 0)
        return -1;
    }
    
    return idx;
  }
  
  protected class Walker
  {
    Point current = new Point(0, 0);
    int indexes[];
    int children[];
    int level = 0;
    
    public Walker()
    {
      indexes = new int[levels];
      indexes[0] = 0;
      children = new int[levels];
      children[0] = 0;      
    }
    
    public void Descend(int child)
    {
      int shift = levels - 1 - level;
      int mask = ~((1 << (shift + 1)) - 1);
      current.x &= mask;
      current.y &= mask;
      current.x |= (child & 1) << shift;
      current.y |= ((child & 2) >> 1) << shift;
    }

    public int GetLevel()
    {
      return level;
    }
    
    public boolean Ascend()
    {
      while(level >= 0 && children[level] == 4)
        --level;

      return level >= 0;      
    }
    
    public int NextChild()
    {
      return children[level]++;
    }
    
    public int ChildIdx(int child)
    {
      return tree.getQuick(indexes[level] + child);
    }
    
    public int GetNodeIdx()
    {
      return indexes[level];
    }
    
    public int NodeSize()
    {
      return 1 << (levels - 1 - level);
    }

    public void JumpLevel(int level)
    {
      this.level = level;
    }
    
    public void OpenNode(int idx)
    {
      ++level;
      indexes[level] = idx;
      children[level] = 0;        
    }

    public Point GetNodePos()
    {
      return current;
    }
    
    public void Reset()
    {
      level = 0;
      children[0] = 0;
      current.x = 0;
      current.y = 0;
    }
    
    public int MinPossibleDistance(int x, int y)
    {
      int minPossibleDistance = 0;
      int nodeSize = NodeSize();
      if(x < current.x)
        minPossibleDistance += current.x - x;
      else if(x >= current.x + nodeSize)
        minPossibleDistance += x - current.x - nodeSize + 1;

      if(y < current.y)
        minPossibleDistance += current.y - y;
      else if(y >= current.y + nodeSize)
        minPossibleDistance += y - current.y - nodeSize + 1;
      
      return minPossibleDistance;
    }
  }
}
