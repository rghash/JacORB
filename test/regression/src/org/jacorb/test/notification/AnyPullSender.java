package org.jacorb.test.notification;

import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.PullSupplierPOA;

import org.jacorb.util.Debug;

import junit.framework.TestCase;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class AnyPullSender extends PullSupplierPOA implements TestClientOperations
{
    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    Any event_;
    Any invalidAny_;
    ProxyPullConsumer myConsumer_;
    boolean connected_ = false;
    boolean available_ = false;
    SupplierAdmin myAdmin_;
    TestCase testCase_;

    public AnyPullSender(TestCase testCase, Any event)
    {
        event_ = event;
        testCase_ = testCase;
    }

    void reset()
    {
        event_ = null;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public void connect(NotificationTestCaseSetup setup,
                        EventChannel channel,
                        boolean useOrSemantic)
    throws AdminLimitExceeded, AlreadyConnected, TypeError, AdminNotFound
    {

        IntHolder _proxyId = new IntHolder();
        IntHolder _adminId = new IntHolder();

        invalidAny_ = setup.getORB().create_any();

        if (useOrSemantic)
        {
            myAdmin_ = channel.new_for_suppliers(InterFilterGroupOperator.OR_OP, _adminId);
            testCase_.assertEquals(InterFilterGroupOperator.OR_OP, myAdmin_.MyOperator());
        }
        else
        {
            myAdmin_ = channel.new_for_suppliers(InterFilterGroupOperator.AND_OP, _adminId);
            testCase_.assertEquals(InterFilterGroupOperator.AND_OP, myAdmin_.MyOperator());
        }

        testCase_.assertEquals(myAdmin_, channel.get_supplieradmin(_adminId.value));

        myConsumer_ =
            ProxyPullConsumerHelper.narrow(myAdmin_.obtain_notification_pull_consumer(ClientType.ANY_EVENT, _proxyId));

        setup.assertEquals(ProxyType._PULL_ANY, myConsumer_.MyType().value());


        myConsumer_.connect_any_pull_supplier(_this(setup.getORB()));
        connected_ = true;
    }

    public void shutdown()
    {
        myConsumer_.disconnect_pull_consumer();
    }

    public void run()
    {
        available_ = true;
    }

    public boolean isEventHandled()
    {
        return sent_;
    }

    boolean sent_ = false;

    public boolean isError()
    {
        return false;
    }

    public void subscription_change(EventType[] e1, EventType[] e2)
    {}

    public Any pull() throws Disconnected
    {
        logger_.info("pull()");

        BooleanHolder _b = new BooleanHolder();
        Any _event;
        while (true)
        {
            _event = try_pull(_b);
            if (_b.value)
            {
                return _event;
            }
            Thread.yield();
        }
    }

    public Any try_pull(BooleanHolder success) throws Disconnected
    {
        logger_.debug("try_pull");

        try
        {
            Any _event = invalidAny_;
            success.value = false;
            if (available_)
            {
                synchronized (this)
                {
                    if (available_)
                    {
                        _event = event_;
                        event_ = null;
                        success.value = true;
                        logger_.debug("try_pull will be successful");
                        sent_ = true;
                        available_ = false;
                    }
                }
            }
            return _event;
        }
        catch (Throwable t)
        {
            logger_.error("during try_pull", t);
            t.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void disconnect_pull_supplier()
    {
        connected_ = false;
    }

}
