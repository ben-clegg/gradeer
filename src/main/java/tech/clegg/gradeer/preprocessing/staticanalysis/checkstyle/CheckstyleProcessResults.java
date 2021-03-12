package tech.clegg.gradeer.preprocessing.staticanalysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.preprocessing.PreProcessorResults;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CheckstyleProcessResults extends PreProcessorResults
{
    private List<String> messages;
    private Collection<AuditEvent> auditEvents;

    private boolean severeError = false;

    public CheckstyleProcessResults()
    {
        super();
        this.messages = new ArrayList<>();
        this.auditEvents = new ArrayList<>();
    }

    public List<String> getMessages()
    {
        return messages;
    }

    protected void addAuditEvent(AuditEvent auditEvent)
    {
        auditEvents.add(auditEvent);
        // Note: audit events have an associated source file that they are reporting for.
        // One source file may have multiple violations.
    }

    public Collection<AuditEvent> getAuditEventsForCheck(CheckstyleCheck checkstyleCheck)
    {
        return auditEvents.stream()
                .filter(ae -> nameMatchesAuditEvent(checkstyleCheck, ae))
                .collect(Collectors.toList());
    }

    public Map<Path, Integer> getSourceViolationCountMapForCheck(CheckstyleCheck checkstyleCheck)
    {
        return generateSourceViolationCountMap(getAuditEventsForCheck(checkstyleCheck));
    }

    private static Map<Path, Integer> generateSourceViolationCountMap(Collection<AuditEvent> auditEventCollection)
    {
        if (auditEventCollection.isEmpty())
            return new HashMap<>();

        Map<Path, Integer> map = new HashMap<>();

        for (AuditEvent ae : auditEventCollection)
        {
            Path source = Paths.get(ae.getFileName());
            map.putIfAbsent(source, 0);

            int existing = map.get(source);
            map.put(source, existing + 1);
        }
        return map;
    }

    private static boolean nameMatchesAuditEvent(CheckstyleCheck checkstyleCheck, AuditEvent auditEvent)
    {
        if(checkstyleCheck.getName() == null || checkstyleCheck.getName().isEmpty())
            return false;
        if(checkstyleCheck.getName().equals(auditEvent.getModuleId()))
            return true;

        // Checkstyle source names are often in the form
        // "com.puppycrawl.tools.checkstyle.checks.whitespace.FileTabCharacterCheck"
        String[] splitSourceName = auditEvent.getSourceName().split("\\.");
        String sourceNameEnding = splitSourceName[splitSourceName.length - 1];
        if(checkstyleCheck.getName().equals(sourceNameEnding) ||
                checkstyleCheck.getName().equals(sourceNameEnding.replace("Check", "")))
            return true;
        return false;
    }

    public boolean hasSevereExecutionErrorOccured()
    {
        return severeError;
    }

    public void setSevereError()
    {
        severeError = true;
    }



}
