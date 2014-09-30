/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jack L (http://jack-l.com)
 */
//Queue linked list
public class QueueLL<D> {

    protected D Data;
    protected QueueLL Next;

    public QueueLL(D Data, QueueLL Next) { //Constructor
        this.Data = Data;
        this.Next = Next;
    }
}
