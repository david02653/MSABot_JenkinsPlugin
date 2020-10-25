package ntou.david.dismessenger;

import groovy.json.internal.IO;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.util.FormValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessagePublisher extends Notifier {

    private final String channel;
    private final String routingKey;
    private final String mqHost;
    private final int mqPort;

    private RabbitControl control = new RabbitControl();

    @DataBoundConstructor
    public MessagePublisher(String channel, String routingKey, String mqHost, int mqPort){
        this.channel = channel;
        this.routingKey = routingKey;
        this.mqHost = mqHost;
        this.mqPort = mqPort;
    }

    public String getChannel(){
        return channel;
    }
    public String getRoutingKey(){
        return routingKey;
    }
    public String getMqHost(){
        return mqHost;
    }
    public int getMqPort(){
        return mqPort;
    }

    private String getResultAsString(Result result){
        String resultStr = "ONGOING";
        if(result != null){
            resultStr = result.toString();
        }
        return resultStr;
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
        listener.getLogger().println("RabbitMQ Host at " + mqHost);
        listener.getLogger().println("RabbitMQ Port is " + mqPort);

        // create JSON format return message
        JSONObject object = new JSONObject();
        object.put("build_name", build.getProject().getName());
        object.put("build_number", build.getNumber());
        object.put("build_status", getResultAsString(build.getResult()));
        object.put("channel", channel);
        object.put("routing_key", routingKey);

        JSONArray tempCase = new JSONArray();
        // method getAggregatedTestResultAction() is deprecated
//        AggregatedTestResultAction resultAction = (AggregatedTestResultAction)build.getAggregatedTestResultAction();
        AggregatedTestResultAction resultAction = build.getAction(AggregatedTestResultAction.class);
        List failList = new ArrayList();
        try{
            failList = resultAction.getFailedTests();
            object.put("fail_count", resultAction.getFailCount());
            object.put("skip_count", resultAction.getSkipCount());
            object.put("total_count", resultAction.getTotalCount());
        }catch (Exception e){
            object.put("fail_count", 0);
            object.put("skip_count", 0);
            object.put("total_count", 0);
        }

        // add failCase in to array object
        for(int i=0; i<failList.size(); i++){
            CaseResult failCase = (CaseResult) failList.get(i);
            JSONObject fail_obj = new JSONObject();
            fail_obj.put("name", failCase.getName());
            fail_obj.put("out", failCase.getStdout());
            tempCase.put(fail_obj);
        }

        final EnvVars env = build.getEnvironment(listener);
        String BUILD_URL = env.get("BUILD_URL");
        object.put("build_url", BUILD_URL);

        listener.getLogger().println("Data : " + build.getRootDir().getAbsolutePath());
        listener.getLogger().println("Build_URL : " + BUILD_URL);

        if(control.sendMessage(object.toString(), routingKey, mqHost, mqPort, listener)){
            listener.getLogger().println("Message Sending Success !");
        }else{
            listener.getLogger().println("Message Sending Failed !");
        }

        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>{
//        public FormValidation doCheckChannel(@QueryParameter String value, @QueryParameter String routingKey) throws IOException, ServletException {
//            if(value.length() == 0){
//                //return FormValidation.error("please enter your target channel");
//                return FormValidation.error(Messages.MessagePublisher_error_missingChannel());
//            }
//            if(routingKey.length() == 0){
//                //return FormValidation.error("no routing found.");
//                return FormValidation.error(Messages.MessagePublisher_error_missingRoutingKey());
//            }
//            return FormValidation.ok();
//        }
        public FormValidation doCheckChannel(@QueryParameter String value) throws IOException, ServletException {
            if(value.length() == 0){
                return FormValidation.error(Messages.MessagePublisher_error_missingChannel());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRoutingKey(@QueryParameter String value) throws IOException, ServletException {
            if(value.length() == 0){
                return FormValidation.error(Messages.MessagePublisher_error_missingRoutingKey());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckMqHost(@QueryParameter String value) throws IOException, ServletException {
            if(value.length() == 0){
                return FormValidation.error(Messages.MessagePublisher_error_missingMQHost());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckMqPost(@QueryParameter String value) throws IOException, ServletException {
            if(value.length() == 0){
                return FormValidation.error(Messages.MessagePublisher_error_missingMQPort());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName(){
            //return "Discord Messenger";
            return Messages.MessagePublisher_DisplayName();
        }
    }
}
