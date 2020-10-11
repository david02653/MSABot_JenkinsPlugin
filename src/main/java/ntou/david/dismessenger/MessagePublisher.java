package ntou.david.dismessenger;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

public class MessagePublisher extends Notifier {

    private final String channel;
    private final String routingKey;

    @DataBoundConstructor
    public MessagePublisher(String channel, String routingKey){
        this.channel = channel;
        this.routingKey = routingKey;
    }

    public String getChannel(){
        return channel;
    }
    public String getRoutingKey(){
        return routingKey;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
//        return super.perform(build, launcher, listener);

        listener.getLogger().println("your channel is "+ channel);
        listener.getLogger().println("your routing key is " + routingKey);

        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>{
        public FormValidation doCheckchannel(@QueryParameter String value) throws IOException, ServletException {
            if(value.length() == 0){
                return FormValidation.error("please enter your target channel");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName(){
            return "Discord Messenger";
        }
    }
}
