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

import java.awt.Point;
import piranha.Map.CellType;
import piranha.Map.Map;
import piranha.Map.Movable;

/**
 *
 * @author ineaugh
 */
public class Passage extends CellType implements ITrigger
{
  String actionName;
  Map destination;
  Point destinationPoint;
  
  public Passage(char symbol, String actionName, Map destination, Point destinationPoint)
  {
    super(symbol, false);
    this.actionName = actionName;
    this.destination = destination;
    this.destinationPoint = new Point(destinationPoint);
  }

  @Override
  public void Activated(Movable byWhom)
  {
    byWhom.MoveToMap(destination, destinationPoint);
  }
}
