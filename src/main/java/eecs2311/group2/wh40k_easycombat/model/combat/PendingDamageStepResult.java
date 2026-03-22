package eecs2311.group2.wh40k_easycombat.model.combat;

public record PendingDamageStepResult(
        boolean applied,
        String message,
        PendingDamage resolvedDamage,
        String targetModelName,
        int appliedDamage,
        int wastedDamage,
        boolean targetDestroyed,
        boolean sessionComplete,
        int remainingPendingCount,
        int remainingPendingDamage,
        CasualtyUpdate casualtyUpdate
) {
    public PendingDamageStepResult {
        message = message == null ? "" : message;
        targetModelName = targetModelName == null ? "" : targetModelName;
        casualtyUpdate = casualtyUpdate == null ? CasualtyUpdate.none() : casualtyUpdate;
    }

    public static PendingDamageStepResult failure(String message) {
        return new PendingDamageStepResult(
                false,
                message,
                null,
                "",
                0,
                0,
                false,
                false,
                0,
                0,
                CasualtyUpdate.none()
        );
    }
}
