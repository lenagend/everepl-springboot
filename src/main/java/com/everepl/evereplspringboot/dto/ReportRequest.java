package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Report;
import com.everepl.evereplspringboot.entity.Target;

public record ReportRequest(Target target, Report.ReportReason reason) {
}
