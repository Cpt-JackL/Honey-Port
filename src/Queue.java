/*
 * Copyright (C) 2014 Jack L (http://jack-l.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author Jack L (http://jack-l.com)
 */
// Queue data structure
public class Queue<D> {

    protected QueueLL Head, Tail;

    public Queue() { //Constructor
        Head = null;
        Tail = null;
    }

    public void En_Queue(D Data) { //enqueue
        if (isEmpty()) { //empty queue
            Head = new QueueLL(Data, null, null);
            Tail = Head;
        } else { //Non empty queue add to the back of the queue
            Tail.Next = new QueueLL(Data, null, Tail);
            Tail = Tail.Next;
        }
    }

    public Object De_Queue() {
        D ReturnData = (D) Head.Data; //Record current data
        if (Head == Tail) { //We de_queueing the last node!!
            Head = null;
            Tail = null;
        } else { //Otherwise...
            Head = Head.Next; //Move head to the next node
            Head.Prev = null;
        }
        return ReturnData; //Return the record
    }

    public Object Peak_Queue() {
        // Works like De_Queue. Returns head data but not remove it from queue
        return Head.Data;
    }

    public boolean Search_Queue(String Data) {
        // String comparison only
        QueueLL CurrentPos = Head;
        while (CurrentPos != null) {
            //See if there is a match
            String CurrentData = CurrentPos.Data.toString();
            if (CurrentData.equals(Data)) {
                return true;
            }

            //Continue search
            CurrentPos = CurrentPos.Next;
        }

        return false;
    }

    public boolean Search_Queue_Backward(String Data) {
        // String comparison only
        QueueLL CurrentPos = Tail;
        while (CurrentPos != null) {
            //See if there is a match
            String CurrentData = CurrentPos.Data.toString();
            if (CurrentData.equalsIgnoreCase(Data)) {
                return true;
            }

            //Continue search
            CurrentPos = CurrentPos.Prev;
        }

        return false;
    }

    public boolean isEmpty() {
        if (Head == null || Tail == null) {
            return true;
        } else {
            return false;
        }
    }
}
