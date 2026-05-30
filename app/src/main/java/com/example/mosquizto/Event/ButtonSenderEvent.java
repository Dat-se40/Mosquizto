package com.example.mosquizto.Event;

import android.widget.ImageButton;

import com.example.mosquizto.Dto.response.CollectionItemResponse;

public class ButtonSenderEvent
{
    public ImageButton sender ; 
    public CollectionItemResponse item ;

    public ButtonSenderEvent(ImageButton sender, CollectionItemResponse item)
    {
        this.sender = sender ;
        this.item = item ;
    }
}
