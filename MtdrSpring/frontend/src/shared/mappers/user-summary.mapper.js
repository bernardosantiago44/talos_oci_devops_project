export function mapUserSummaryFromSource(source) {
    const id = source.authorUserId ??
        source.actorUserId ??
        source.userId ??
        '';
    const name = source.authorName ??
        source.actorName ??
        source.userName ??
        '';
    const email = source.authorEmail ??
        source.actorEmail ??
        source.userEmail ??
        null;
    const telegramUserId = source.authorTelegramUserId ??
        source.actorTelegramUserId ??
        source.userTelegramUserId ??
        null;
    return {
        id,
        name,
        email,
        telegramUserId,
    };
}
