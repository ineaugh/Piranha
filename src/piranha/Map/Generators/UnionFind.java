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

class UnionFind
{
  TIntArrayList rank = new TIntArrayList();
  TIntArrayList parent = new TIntArrayList();
  
  int Add()
  {
    int newVal = parent.size();
    parent.add(newVal);
    rank.add(0);
    return newVal;
  }

//
// Find procedure
//
  int FindRoot(int k)
  {
    int p = parent.getQuick(k);
    if (p != k)
    {
      p = FindRoot(p);
      parent.setQuick(k, p);
    }

    return p;
  }

//
// Assume x and y being roots
//
  void UnionRoot(int x, int y)
  {
    if(x == y)
      return;
    
    int xRank = rank.getQuick(x), yRank = rank.getQuick(y);
    if(xRank < yRank)
      parent.setQuick(x, y);
    else
    {
      parent.setQuick(y, x);
      if(xRank == yRank)
        rank.setQuick(x, rank.getQuick(x) + 1);
    }      
  }
  
  int GetNumIDs() { return parent.size(); }
  
  public void Clear()
  {
    parent.clear();
    rank.clear();
  }
}
