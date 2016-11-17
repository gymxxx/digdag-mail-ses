package io.digdag.plugin.mail;


import com.amazonaws.auth.BasicAWSCredentials;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigException;
import io.digdag.spi.*;
import io.digdag.util.BaseOperator;

import java.nio.file.Path;
import java.util.List;
import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;
import com.amazonaws.regions.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AmazonSESOperatorFactory implements OperatorFactory {
    private final TemplateEngine templateEngine;

    @Inject
    AmazonSESOperatorFactory(TemplateEngine templateEngine)
    {
        this.templateEngine = templateEngine;
    }
    @Override
    public String getType() {
        return "mail_ses";
    }

    @Override
    public Operator newOperator(Path projectPath, TaskRequest request) {
        return new AmazonSESOperator(projectPath, request);
    }

    private class AmazonSESOperator
            extends BaseOperator
    {

        AmazonSESOperator(Path projectPath, TaskRequest request)
        {
            super(projectPath, request);
        }

        @Override
        public List<String> secretSelectors()
        {
            return ImmutableList.of("aws.*");
        }

        @Override
        public TaskResult runTask(TaskExecutionContext ctx)
        {
            SecretProvider secrets = ctx.secrets();
            String key = secrets.getSecret("aws.key");
            String secret = secrets.getSecret("aws.secret");

            Config params = request.getConfig().mergeDefault(
                    request.getConfig().getNestedOrGetEmpty("aws_ses"));

            String body = workspace.templateCommand(templateEngine, params, "body", UTF_8);
            String subject = params.get("subject", String.class, "(No subject)");
            String from = params.get("sender", String.class, "");

            List<String> toList;
            try {
                toList = params.getList("to", String.class);
            }
            catch (ConfigException ex) {
                toList = ImmutableList.of(params.get("to", String.class));
            }

            boolean isHtml = params.get("use_html", boolean.class, false);

            // Construct an object to contain the recipient address.
            Destination destination = new Destination().withToAddresses(toList.toArray(new String[0]));

            // Create the subject and body of the message.
            Content title = new Content().withData(subject);
            Content contentBody = new Content().withData(body);
            Body content = new Body();

            if(isHtml) {
                content = content.withHtml(contentBody);
            } else {
                content = content.withText(contentBody);
            }

            // Create a message with the specified subject and body.
            Message message = new Message().withSubject(title).withBody(content);

            // Assemble the email.
            SendEmailRequest sendEmailRequest = new SendEmailRequest().withSource(from).withDestination(destination).withMessage(message);

            try
            {
                System.out.println("Attempting to send an email through Amazon SES...");
                // For more information, see http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
                BasicAWSCredentials awsCreds = new BasicAWSCredentials(key, secret);
                AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(awsCreds);

                // and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
                Region REGION = Region.getRegion(Regions.US_EAST_1);
                client.setRegion(REGION);

                // Send the email.
                client.sendEmail(sendEmailRequest);
                System.out.println("Email sent!");
            }
            catch (Exception ex)
            {
                throw new TaskExecutionException(ex, TaskExecutionException.buildExceptionErrorConfig(ex));
            }

            return TaskResult.empty(request);
        }
    }
}
