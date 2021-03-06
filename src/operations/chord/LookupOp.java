package operations.chord;

import messages.Message;
import messages.chord.Lookup;
import messages.chord.LookupReply;
import peer.Peer;
import peer.chord.ChordPeer;
import peer.chord.ChordReference;
import peer.ssl.SSLConnection;

import java.nio.charset.StandardCharsets;

public class LookupOp extends ChordOperation {
    public LookupOp(SSLConnection connection, Lookup message, Peer context) {
        super(connection, message, context);
    }

    @Override
    public void run() {
        int target = ((Lookup) this.message).getTarget();

        log.debug("Started Lookup for:" + target);
        ChordReference self = context.getReference();
        ChordReference closest;

        if (context.successor() == null) {
            closest = self;
        } else if (ChordPeer.between(target, self.getGuid(), context.successor().getGuid(), false)) {
            closest = context.successor();
        } else {
            closest = context.closestPrecedingNode(target);
            closest = context.findSuccessor(closest, target);
        }

        log.debug("Sending closest peer: " + closest);

        Message message = new LookupReply(context.getReference(), closest.toString().getBytes(StandardCharsets.UTF_8));

        context.send(this.connection, message);
    }
}
