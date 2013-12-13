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
import java.util.List;

/**
 *
 * @author ineaugh
 */
public class Passage
{
  public static interface Maker
  {
    void MakePassage(Maze maze, LineSegment where);
  }
  
  public static void MakeReachable(Maze maze, Maker maker)
  {
    List<Room> rooms = new ArrayList<>();
    UnionFind union = new UnionFind();
    
    while(true)
    {
      rooms.clear();
      PointMapInt borderMap = maze.GetRooms(rooms);
      if(rooms.size() < 2)
        return;
      
      union.Clear();
      for(int i = 0; i < rooms.size(); ++i)
        union.Add();
      
      List<PointMapInt.EntryConnection> roomConnections = borderMap.FindClosestConnections();
      for(PointMapInt.EntryConnection c : roomConnections)
      {
        int root1 = union.FindRoot(c.beginValue), root2 = union.FindRoot(c.endValue);
        if(root1 != root2)
        {
          maker.MakePassage(maze, c.connection);
          union.UnionRoot(root1, root2);
        }
      }
    }
  }
}
