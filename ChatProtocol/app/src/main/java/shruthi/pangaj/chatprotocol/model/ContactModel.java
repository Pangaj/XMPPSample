package shruthi.pangaj.chatprotocol.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pangaj on 31/03/17.
 */
public class ContactModel {

    private static ContactModel sContactModel;
    private List<Contact> mContacts;

    public static ContactModel get(Context context)
    {
        if(sContactModel == null)
        {
            sContactModel = new ContactModel(context);
        }
        return  sContactModel;
    }

    private ContactModel(Context context)
    {
        mContacts = new ArrayList<>();
    }

    public List<Contact> getContacts()
    {
        return mContacts;
    }

}
