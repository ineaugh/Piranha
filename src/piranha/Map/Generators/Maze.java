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
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TIntLongProcedure;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import squidpony.squidgrid.util.Direction;

/**
 *
 * @author ineaugh
 */
public class Maze
{
  boolean passability[][];
  
  public Maze(Dimension size)
  {
    this(size.width, size.height);
  }
  
  public Maze(int width, int height)
  {
    passability = new boolean[width][height];
    
    for(int x = 0; x < width; ++x)    
      for(int y = 0; y < height; ++y)
        passability[x][y] = false;
  }
  
  public int GetWidth() { return passability.length; }
  public int GetHeight() { return passability[0].length; }
  
  public boolean Get(int x, int y) { return passability[x][y]; }
  public boolean Get(Point p) { return passability[p.x][p.y]; }
  public void Set(int x, int y, boolean passable) { passability[x][y] = passable; }
  public void Set(Point p, boolean passable) { passability[p.x][p.y] = passable; }
  public boolean GetSafe(int x, int y) { return IsIn(x, y) && passability[x][y]; }
  public boolean IsIn(int x, int y) { return x >= 0 && x < GetWidth() && y >= 0 && y < GetHeight(); }
  public void SetSafe(int x, int y, boolean passable)
  {
    if(IsIn(x, y))
      passability[x][y] = passable;
  } 
  
  public PointSet GetFreeCells()
  {
    int width = passability.length, height = passability[0].length;
    PointSet set = new PointSet(Math.max(width, height));
    for(int x = 0; x < width; ++x)    
      for(int y = 0; y < height; ++y)
        if(passability[x][y])    
          set.Add(x, y);
    
    return set;
  }  
  
  public PointMapInt GetRooms(List<Room> allRooms)
  {
    int width = passability.length, height = passability[0].length;
    int extent = Math.max(width, height);
    int ids[][] = new int[width][height];
    UnionFind idsUnion = new UnionFind();
    
    // first pass
    for(int y = 0; y < height; ++y)
      for(int x = 0; x < width; ++x)    
        if(passability[x][y])
        {
          int label = -1;
          if(y > 0 && passability[x][y - 1])
            label = ids[x][y - 1];
          
          if(x > 0 && passability[x - 1][y] && label != ids[x - 1][y])
            if(label == -1)
              label = ids[x - 1][y];
            else
              idsUnion.UnionRoot(idsUnion.FindRoot(label), idsUnion.FindRoot(ids[x - 1][y]));
          
          if(label == -1)
            label = idsUnion.Add();
          
          ids[x][y] = label;
        }
        else
          ids[x][y] = -1;
    
    Room roomsArray[] = new Room[idsUnion.GetNumIDs()];
    int roomIdx[] = new int[idsUnion.GetNumIDs()];
    PointMapInt borders = new PointMapInt(extent);
    int id = 0;
    
    // second pass
    for(int y = 0; y < height; ++y)
      for(int x = 0; x < width; ++x) 
        if(ids[x][y] != -1)
        {
          int root = idsUnion.FindRoot(ids[x][y]);
          if(roomsArray[root] == null)
          {
            roomsArray[root] = new Room();
            roomIdx[root] = id++;
            allRooms.add(roomsArray[root]);
          }
          
          roomsArray[root].GetCentroid().translate(x, y);
          roomsArray[root].SetSize(roomsArray[root].GetSize() + 1);
          
          if(x > 0 && !passability[x - 1][y] || x < width - 1 && !passability[x + 1][y] || y > 0 && !passability[x][y - 1] || y < height - 1 && !passability[x][y + 1])
            borders.Add(x, y, roomIdx[root]);
        }
        
    for(Room ps : allRooms)
    {
      ps.GetCentroid().x /= ps.GetSize();
      ps.GetCentroid().y /= ps.GetSize();
    }
    
    return borders;
  }
  
  public void CheckerBoard(int square)
  {
    int width = passability.length, height = passability[0].length;
    
    for(int x = 0; x < width; ++x)    
      for(int y = 0; y < height; ++y)
        passability[x][y] = !(x % square == 0 || y % square == 0 || (x / square + y / square) % 2 == 0); 
  }  
  

  
  void SetHorizontalLine(int x0, int x1, int y, boolean value)
  {
    if(x0 > x1)
    {
      int x = x0;
      x0 = x1;
      x1 = x;
    }
    
    for(; x0 <= x1; ++x0)
      SetSafe(x0, y, value);
  }  
  
  void Set8(int ox, int oy, int x, int y, boolean value)
  {
    SetHorizontalLine(ox + x, ox - x, oy + y, value);
    SetHorizontalLine(ox + x, ox - x, oy - y, value);
    SetHorizontalLine(ox + y, ox - y, oy + x, value);
    SetHorizontalLine(ox + y, ox - y, oy - x, value);
  } 
  
  public void AddCircle(int ox, int oy, int radius, boolean value)
  {
    int rs2 = radius * radius * 4; /* this could be folded into ycs2 */
    int xs2 = 0;
    int ys2m1 = rs2 - 2*radius + 1;
    int x = 0;
    int y = radius;
    int ycs2;
    Set8(ox, oy, x, y, value);
    while(x <= y) 
    {
      /* advance to the right */
      xs2 = xs2 + 8*x + 4;
      ++x;
      /* calculate new Yc */
      ycs2 = rs2 - xs2;
      if(ycs2 < ys2m1) 
      {
        ys2m1 = ys2m1 - 8*y + 4;
        --y;
      }
      
      Set8(ox, oy, x, y, value);
    }
  }  
  
  public void AddBorder(boolean value)
  {
    SetHorizontalLine(0, GetWidth() - 1, 0, value);
    SetHorizontalLine(0, GetWidth() - 1, GetHeight() - 1, value);
    
    for(int y = 0; y < GetHeight(); ++y)
    {
      passability[0][y] = value;    
      passability[GetWidth() - 1][y] = value;    
    }
  }
  
  long CoordToLong(int x, int y) { return (long)x | (((long)y) << Integer.SIZE); }
  void LongToCoord(long c, Point p)
  {
    p.x = (int)(c & ((((long) 1) << (Integer.SIZE + 1)) - 1));
    p.y = (int)(c >> Integer.SIZE);
  }
  
  
  public void FillDistances(Point from, float[][] distances)
  {
    int width = passability.length, height = passability[0].length;
    
    for(int x = 0; x < width; ++x)    
      for(int y = 0; y < height; ++y)
        distances[x][y] = Float.POSITIVE_INFINITY;
    
    if(!passability[from.x][from.y])
      return;
    
    distances[from.x][from.y] = 0;
    TLongArrayList visited = new TLongArrayList(), justVisited = new TLongArrayList();
    TIntArrayList visitedDir = new TIntArrayList(), justVisitedDir = new TIntArrayList();

    for(int i = 0; i < Utils.ClockwiseDirs.length; ++i)
      VisitPoint(from, distances, visited, visitedDir, i);
    
    Point p = new Point();
    while(!visited.isEmpty())
    {
      for(int i = 0; i < visited.size(); ++i)
      {
        LongToCoord(visited.getQuick(i), p);
        int di = visitedDir.getQuick(i);
        VisitPoint(p, distances, justVisited, justVisitedDir, di);
        VisitPoint(p, distances, justVisited, justVisitedDir, (di + 1) % Utils.ClockwiseDirs.length);
        VisitPoint(p, distances, justVisited, justVisitedDir, (di - 1 + Utils.ClockwiseDirs.length) % Utils.ClockwiseDirs.length);
        if(di % 2 == 1)
        {
          VisitPoint(p, distances, justVisited, justVisitedDir, (di + 2) % Utils.ClockwiseDirs.length);
          VisitPoint(p, distances, justVisited, justVisitedDir, (di - 2 + Utils.ClockwiseDirs.length) % Utils.ClockwiseDirs.length);
        }
      }
      
      TLongArrayList jv = justVisited;
      TIntArrayList jvd = justVisitedDir;
      justVisited = visited;
      justVisitedDir = visitedDir;
      visited = jv;
      visitedDir = jvd;
      justVisited.resetQuick();
      justVisitedDir.resetQuick();
    }
  }

  void VisitPoint(Point from, float[][] distances, TLongArrayList visited, TIntArrayList visitedDir, int i)
  {
    Direction d = Utils.ClockwiseDirs[i];          
    if(GetSafe(from.x + d.deltaX, from.y + d.deltaY) && distances[from.x + d.deltaX][from.y + d.deltaY] == Float.POSITIVE_INFINITY)
    {
      distances[from.x + d.deltaX][from.y + d.deltaY] = distances[from.x][from.y] + 1;
      visited.add(CoordToLong(from.x + d.deltaX, from.y + d.deltaY));
      visitedDir.add(i);
    }
  }
}
