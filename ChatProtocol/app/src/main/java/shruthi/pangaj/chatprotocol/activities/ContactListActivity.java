package shruthi.pangaj.chatprotocol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;

import shruthi.pangaj.chatprotocol.R;
import shruthi.pangaj.chatprotocol.model.Contact;
import shruthi.pangaj.chatprotocol.rooster.RoosterConnection;

/**
 * Created by Pangaj on 31/03/17.
 */

public class ContactListActivity extends AppCompatActivity {

    private static final String TAG = "ContactListActivity";

    private RecyclerView contactsRecyclerView;
    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        contactsRecyclerView = (RecyclerView) findViewById(R.id.contact_list_recycler_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

//        callContactDetails();

        TextView tvGroupChat = (TextView) findViewById(R.id.tv_group_chat);
        tvGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactListActivity.this, ChatActivity.class);
                intent.putExtra("EXTRA_CONTACT_JID", "channelgroup@conference.lzoom.com");
                intent.putExtra("EXTRA_CONTACT_JID_WITH_RESOURCE", "channelgroup@conference.lzoom.com");
                startActivity(intent);
            }
        });
    }


    private class ContactHolder extends RecyclerView.ViewHolder {
        private TextView contactTextView;
        private Contact mContact;

        public ContactHolder(View itemView) {
            super(itemView);

            contactTextView = (TextView) itemView.findViewById(R.id.contact_jid);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Inside here we start the chat activity
                    Intent intent = new Intent(ContactListActivity.this, ChatActivity.class);
                    intent.putExtra("EXTRA_CONTACT_JID", mContact.getJid());
                    intent.putExtra("EXTRA_CONTACT_JID_WITH_RESOURCE", mContact.getJidWithResource());
                    startActivity(intent);


                }
            });
        }


        public void bindContact(Contact contact) {
            mContact = contact;
            if (mContact == null) {
                Log.d(TAG, "Trying to work on a null Contact object ,returning.");
                return;
            }
            contactTextView.setText(mContact.getJid());

        }
    }


    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {
        private ArrayList<Contact> mContacts;

        public ContactAdapter(ArrayList<Contact> contactList) {
            mContacts = contactList;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.list_item_contact, parent,
                            false);
            return new ContactHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact contact = mContacts.get(position);
            holder.bindContact(contact);

        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
//            callContactDetails();

        }
        return true;
    }

    /*private void callContactDetails() {
        Roster roster = RoosterConnection.getConnection().getRoster();
        Collection<RosterEntry> entries = roster.getEntries();
        Presence presence;
        ArrayList<Contact> contacts = new ArrayList<>();
        for (RosterEntry entry : entries) {
            presence = roster.getPresence(entry.getUser());
            if(presence.getType().name().equalsIgnoreCase("available")) {
                Contact contact = new Contact(entry.getUser(), presence.getFrom());
                System.out.println(entry.getUser());
                System.out.println(presence.getType().name());
                System.out.println(presence.getFrom());
                System.out.println(presence.getStatus());
                contacts.add(contact);
            }
        }
        if(contacts.size() > 0) {
            mAdapter = new ContactAdapter(contacts);
            contactsRecyclerView.setAdapter(mAdapter);
        }
    }*/
}
