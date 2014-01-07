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

package piranha.Map.Places;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import piranha.Map.Generators.Maze;
import piranha.Map.Generators.Passage;
import piranha.Map.Generators.Simple;
import piranha.Map.Generators.Utils;
import piranha.Map.Map;

/**
 *
 * @author ineaugh
 */
public class Circles extends PlaceBase
{
  public Maze Initialize(Dimension size, Random rand, List<Point> wantedSpecialPoints)
  {
    super.Initialize(size);
    Maze maze = new Maze(GetWidth(), GetHeight());
    List<Point> bases = new ArrayList<>();
    int area = GetWidth() * GetHeight();
    GenCircles(maze, rand, area / 1000, 3, 10, true, bases);
    GenCircles(maze, rand, area / 5000, 10, 15, false);
    for(Point p : wantedSpecialPoints)
      GenCircle(maze, p.x, p.y, rand, 3, 10, true);
    
    bases.addAll(wantedSpecialPoints);
    
    maze.AddBorder(false);
    Passage.MakeReachable(maze, new Simple.StraightPassageMaker()); 
    Utils.SetBinaryMap(maze, this);
    
    for(Point p : bases)
      if(maze.Get(p))
        specialPoints.add(p);
    
    return maze;
  }
  
  public static void GenCircles(Maze maze, Random rand, int numCircles, int fromRadius, int toRadius, boolean value)
  {
    GenCircles(maze, rand, numCircles, fromRadius, toRadius, value, null);
  }
  
  public static void GenCircles(Maze maze, Random rand, int numCircles, int fromRadius, int toRadius, boolean value, List<Point> centers)
  {
    for(int i = 0; i < numCircles; ++i)
    {
      int x = rand.nextInt(maze.GetWidth()), y = rand.nextInt(maze.GetHeight());
      GenCircle(maze, x, y, rand, fromRadius, toRadius, value);
      if(centers != null)
        centers.add(new Point(x, y));
    }
  }   

  static void GenCircle(Maze maze, int x, int y, Random rand, int fromRadius, int toRadius, boolean value)
  {
    maze.AddCircle(x, y, rand.nextInt(toRadius - fromRadius + 1) + fromRadius, value);
  }
}
