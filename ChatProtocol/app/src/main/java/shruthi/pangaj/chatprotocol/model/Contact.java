package shruthi.pangaj.chatprotocol.model;

/**
 * Created by Pangaj on 31/03/17.
 */
public class Contact {
    private String jid;
    private String jidWithResource;

    public Contact(String contactJid, String contactJidWithResource) {
        jid = contactJid;
        jidWithResource = contactJidWithResource;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getJidWithResource() {
        return jidWithResource;
    }

    public void setJidWithResource(String jidWithResource) {
        this.jidWithResource = jidWithResource;
    }
}
