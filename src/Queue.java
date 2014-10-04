/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            Head = new QueueLL(Data, null);
            Tail = Head;
        } else { //Non empty queue add to the back of the queue
            Tail.Next = new QueueLL(Data, null);
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
        }
        return ReturnData; //Return the record
    }

    public Object Peak_Queue() {
        // Works like De_Queue. Returns head data but not remove it from queue
        return Head.Data;
    }

    public boolean Search_Queue(String Data) {
        // Empty Queue
        if (isEmpty()) {
            return false;
        }

        // String comparison only
        QueueLL CurrentPos = Head;
        while (CurrentPos != null) {
            //See if there is a match
            String CurrentData = CurrentPos.Data.toString();
            if (CurrentData.equalsIgnoreCase(Data)) {
                return true;
            }

            //Continue search
            CurrentPos = CurrentPos.Next;
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
