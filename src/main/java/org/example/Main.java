package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, EmbedData> pendingEmbeds = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> userMessageTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Integer> userWarnings = new ConcurrentHashMap<>();

    private static final String GUILD_ID = "1449061779060687063";
    private static final String VERIFY_CHANNEL_ID = "1464627654744477819";
    private static final String VERIFIED_ROLE_ID = "1464623361626734637";
    private static final String OLHEIRO_ROLE_ID = "1449070067131224268";
    private static final String SCRIM_HOSTER_ROLE_ID = "1449070133040517262";
    private static final String FA_ALLOWED_CHANNEL = "1461773344620941534";
    private static final String PENEIRA_CHANNEL = "1449069785601282068";
    private static final String SCRIM_CHANNEL = "1450548729303011379";
    private static final String LOG_CHANNEL_ID = "1449525327175880865";
    private static final String GFX_CHANNEL_ID = "1461773344620941534";

    private static final List<String> AMIS_CHANNELS = Arrays.asList(
            "1449070534934401044",
            "1449070508816728198",
            "1449070445327421682",
            "1457070154226602208"
    );

    private static final String SORTEIO_CANAL_LINK = "https://discord.com/channels/1449061779060687063/1449115997804957806";
    private static final String VERIFY_CHANNEL_LINK = "https://discord.com/channels/" + GUILD_ID + "/" + VERIFY_CHANNEL_ID;
    private static final String CUSTOM_ICON = "https://images-ext-1.discordapp.net/external/JqpzlTlIxaM5Yl53AY7hH09Tl8DK5k9lMwwt7oKHIhw/%3Fformat%3Dwebp%26quality%3Dlossless/https/images-ext-1.discordapp.net/external/_7vAzr-RdzvNO08YWoiLiFqxr_3rAymqm_V_R1-jiT4/%253Fsize%253D2048/https/cdn.discordapp.com/icons/1449061779060687063/ecbd3ce76f39128b1ec08154e7faff75.png?format=webp&quality=lossless";

    private static final List<String> ALLOWED_LINK_CHANNELS = Arrays.asList(
            "1449070133778714738", "1449112362912186389", "1453095863823110184", "1449071892873871522"
    );

    private static final List<String> GFX_KEYWORDS = Arrays.asList(
            "gfx gratis", "gfx gratuito", "faço gfx", "fazer gfx", "faça gfx",
            "gfx pago", "vendo gfx", "compro gfx", "preciso de gfx", "quer gfx",
            "gfx"
    );

    private static final List<String> AMIS_KEYWORDS = Arrays.asList(
            "algm amis", "alguem amis", "alguém amis", "amis",
            "my host", "lf my host", "lf host", "time bom",
            "meu host", "bola personalizada", "alguém pra jogar",
            "amistoso", "10 robux", "valendo robux", "vs", "versus"
    );

    private static final List<String> SPAM_KEYWORDS = Arrays.asList(
            "gravação", "gravar comigo", "participar de uma gravação",
            "robux gratis", "robux free", "free robux",
            "league grátis", "criar uma league", "criar liga",
            "impresa", "nacional tcs", "responder questionário",
            "tiktok.com", "tik tok", "vm.tiktok", "vt.tiktok",
            "busco editor", "procuro editor", "editor de fotos", "editor de video", "editor de vídeo",
            "alguém pra ownar", "alg pra ownar", "preciso de owner", "busco owner",
            "falta", "membros pra", "criar logo", "logo de league", "logo gratis", "logo de graça", "técnico de league", "procuro técnico", "busco técnico"
    );

    private static final List<String> PROFANITY = Arrays.asList(
            "porra", "caralho", "fdp", "buceta", "merda", "puta", "arrombado", "cuzão",
            "puta que pariu", "vai tomar no cu", "filho da puta", "vsf", "vtnc",
            "cu", "puta merda", "vai se foder", "foda-se", "foda", "cacete",
            "desgraça", "desgraçado", "otário", "idiota", "imbecil", "burro",
            "retardado", "mongol", "bosta", "corno", "viado", "gay", "bicha",
            "traveco", "sapatão", "vagabunda", "prostituta", "piranha", "vadia",
            "putaria", "pornografia", "punheta", "broxa", "penis", "vagina",
            "pau", "pinto", "rola", "pirocão", "piroca", "xoxota", "xereca",
            "bucetão", "cuzão", "rabo", "bunda", "peito", "teta", "mama"
    );

    private static final long DELAY_PER_MESSAGE_MS = 1500;

    public static void main(String[] args) {
        String token = System.getenv("TOKEN");

        try {
            JDABuilder.createDefault(token)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_PRESENCES
                    )
                    .addEventListeners(new Main())
                    .build();
            logger.info("Bot iniciado com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao iniciar o bot", e);
        }
    }

    private boolean isAdmin(MessageReceivedEvent event) {
        if (event.getMember() == null) return false;
        return event.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    private void sendWarningDM(Member member, String reason, String action) {
        member.getUser().openPrivateChannel().queue(
                privateChannel -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("⚠️ Aviso de Moderação - Pafo")
                            .setDescription("Você recebeu uma advertência no servidor.")
                            .addField("📋 Motivo", reason, false)
                            .addField("⚡ Ação Tomada", action, false)
                            .addField("📌 Dica", "Por favor, leia as regras do servidor para evitar futuras punições.", false)
                            .setColor(Color.ORANGE)
                            .setThumbnail(CUSTOM_ICON)
                            .setFooter("Sistema AutoMod • Pafo", CUSTOM_ICON)
                            .setTimestamp(Instant.now());

                    privateChannel.sendMessageEmbeds(embed.build()).queue(
                            success -> logger.info("DM de aviso enviada para: {}", member.getUser().getName()),
                            error -> logger.warn("Não foi possível enviar DM para: {}", member.getUser().getName())
                    );
                },
                error -> logger.warn("Não foi possível abrir canal privado com: {}", member.getUser().getName())
        );
    }

    private void logPunishment(Guild guild, Member member, String reason, String action) {
        TextChannel logChannel = guild.getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel != null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🛡️ Registro de Punição")
                    .setDescription("Um usuário violou as regras do servidor.")
                    .addField("👤 Usuário", member.getAsMention() + " (" + member.getId() + ")", false)
                    .addField("📋 Motivo", reason, false)
                    .addField("⚡ Ação", action, false)
                    .setColor(Color.RED)
                    .setThumbnail(member.getUser().getAvatarUrl())
                    .setFooter("Log Automático • Pafo", CUSTOM_ICON)
                    .setTimestamp(Instant.now());

            logChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    private void sendTemporaryWarning(TextChannel channel, Member member, String title, String description, Color color) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(member.getAsMention() + " " + description)
                .setColor(color)
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Sistema AutoMod • Pafo", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue(msg -> {
            scheduler.schedule(() -> {
                msg.delete().queue(
                        success -> logger.info("Embed de aviso deletado após 1 minuto"),
                        error -> logger.warn("Erro ao deletar embed de aviso")
                );
            }, 60, TimeUnit.SECONDS);
        });
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("roblox_modal")) {
            var input = event.getValue("roblox_username");
            String robloxUsername = (input != null) ? input.getAsString() : "Desconhecido";

            Member member = event.getMember();

            if (member == null) {
                event.reply("❌ Erro ao processar: Membro não encontrado.").setEphemeral(true).queue();
                return;
            }

            String currentNick = member.getEffectiveName();
            String newNick = currentNick + " (@" + robloxUsername + ")";

            if (newNick.length() > 32) {
                newNick = newNick.substring(0, 32);
            }

            String finalNick = newNick;
            member.modifyNickname(finalNick).queue(
                    success -> {
                        addRoleAndReply(event, member, finalNick, true);
                        logger.info("Verificado com Roblox: {} -> {}", member.getUser().getName(), robloxUsername);
                    },
                    error -> {
                        addRoleAndReply(event, member, finalNick, false);
                    }
            );
        }
    }

    private void addRoleAndReply(ModalInteractionEvent event, Member member, String newNick, boolean nickChanged) {
        Role verifiedRole = event.getGuild().getRoleById(VERIFIED_ROLE_ID);

        if (verifiedRole != null) {
            event.getGuild().addRoleToMember(member, verifiedRole).queue();
        } else {
            logger.warn("Cargo de verificação não encontrado! ID: " + VERIFIED_ROLE_ID);
        }

        if (nickChanged) {
            event.reply("""
                    ✅ **VERIFICADO COM SUCESSO!** 🎉
                    
                    **Seu novo apelido:** %s
                    
                    Você agora tem acesso total ao servidor! 🚀
                    """.formatted(newNick)).setEphemeral(true).queue();
        } else {
            event.reply("""
                    ✅ **VERIFICADO!** 🎉
                    
                    (Não consegui alterar seu apelido por falta de permissão ou hierarquia, mas você está verificado!)
                    Você pode alterar manualmente para: %s
                    """.formatted(newNick)).setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.equals("verify_button")) {
            Member member = event.getMember();

            if (member == null) {
                event.reply("❌ Erro ao verificar membro.").setEphemeral(true).queue();
                return;
            }

            Role verifiedRole = event.getGuild().getRoleById(VERIFIED_ROLE_ID);

            if (verifiedRole != null && member.getRoles().contains(verifiedRole)) {
                event.reply("✅ Você já está verificado!").setEphemeral(true).queue();
                return;
            }

            TextInput robloxInput = TextInput.create("roblox_username", "Nome de usuário do Roblox", TextInputStyle.SHORT)
                    .setPlaceholder("Digite seu usuário do Roblox")
                    .setRequired(true)
                    .setMaxLength(20)
                    .build();

            Modal modal = Modal.create("roblox_modal", "🎮 Verificação Roblox")
                    .addActionRow(robloxInput)
                    .build();

            event.replyModal(modal).queue();
        }
        else if (componentId.equals("go_sorteio")) {
            event.reply("🎁 **Link do Sorteio:**\n" + SORTEIO_CANAL_LINK + "\n\nBoa sorte! 🍀").setEphemeral(true).queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String messageRaw = event.getMessage().getContentRaw();
        String messageLower = messageRaw.toLowerCase();
        String userId = event.getAuthor().getId();
        String channelId = event.getChannel().getId();

        if (!isAdmin(event)) {

            long now = System.currentTimeMillis();
            userMessageTimestamps.putIfAbsent(userId, new ArrayList<>());
            List<Long> times = userMessageTimestamps.get(userId);
            times.add(now);
            times.removeIf(t -> now - t > 5000);

            if (times.size() >= 5) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(
                        event.getChannel().asTextChannel(),
                        event.getMember(),
                        "⚠️ SPAM DETECTADO",
                        "**Pare de enviar mensagens tão rápido!**\n\n> Você enviou 5+ mensagens em 5 segundos.\n> Aguarde alguns segundos antes de enviar novamente.",
                        Color.ORANGE
                );
                sendWarningDM(event.getMember(), "Spam de mensagens (5+ mensagens em 5 segundos)", "Mensagem deletada + Aviso");
                logPunishment(event.getGuild(), event.getMember(), "Spam de mensagens (5+ msg em 5s)", "Mensagem Deletada + Aviso");
                return;
            }

            for (String word : PROFANITY) {
                if (messageLower.matches(".*\\b" + word + "\\b.*")) {
                    event.getMessage().delete().queue();

                    int warnings = userWarnings.getOrDefault(userId, 0) + 1;
                    userWarnings.put(userId, warnings);

                    if (warnings >= 3) {
                        if (event.getMember() != null) {
                            event.getMember().timeoutFor(Duration.ofHours(1)).queue();
                        }
                        sendTemporaryWarning(
                                event.getChannel().asTextChannel(),
                                event.getMember(),
                                "🚫 SILENCIADO POR 1 HORA",
                                "**Você atingiu 3 avisos por linguagem inadequada!**\n\n> ⏰ Timeout de **1 hora** aplicado.\n> 📖 Leia as regras do servidor.",
                                Color.RED
                        );
                        sendWarningDM(event.getMember(), "Linguagem inadequada (3º aviso)", "Timeout de 1 hora aplicado");
                        logPunishment(event.getGuild(), event.getMember(), "Linguagem inadequada (3x)", "Timeout 1h");
                        userWarnings.put(userId, 0);
                    } else {
                        sendTemporaryWarning(
                                event.getChannel().asTextChannel(),
                                event.getMember(),
                                "⚠️ AVISO " + warnings + "/3",
                                "**Linguagem inadequada detectada!**\n\n> 🚨 Próximo aviso resultará em **timeout de 1 hora**.\n> 📝 Evite usar palavrões.",
                                Color.ORANGE
                        );
                        sendWarningDM(event.getMember(), "Linguagem inadequada", "Aviso " + warnings + "/3 - Mensagem deletada");
                        logPunishment(event.getGuild(), event.getMember(), "Linguagem inadequada", "Aviso " + warnings + "/3");
                    }
                    return;
                }
            }

            for (String gfxKey : GFX_KEYWORDS) {
                if (messageLower.contains(gfxKey)) {
                    if (!channelId.equals(GFX_CHANNEL_ID)) {
                        event.getMessage().delete().queue();
                        sendTemporaryWarning(
                                event.getChannel().asTextChannel(),
                                event.getMember(),
                                "❌ CANAL INCORRETO DE GFX",
                                "**Pedidos ou vendas de GFX não são permitidos aqui!**\n\n> 📍 Use o canal correto: <#" + GFX_CHANNEL_ID + ">",
                                Color.RED
                        );
                        sendWarningDM(event.getMember(), "Divulgação de GFX em canal incorreto", "Mensagem deletada");
                        logPunishment(event.getGuild(), event.getMember(), "GFX em canal incorreto", "Mensagem Deletada");
                        return;
                    }
                }
            }

            for (String amisKey : AMIS_KEYWORDS) {
                if (messageLower.contains(amisKey)) {
                    if (channelId.equals(FA_ALLOWED_CHANNEL)) {
                        continue;
                    }

                    if (!AMIS_CHANNELS.contains(channelId)) {
                        event.getMessage().delete().queue();
                        sendTemporaryWarning(
                                event.getChannel().asTextChannel(),
                                event.getMember(),
                                "❌ CANAL INCORRETO DE AMIS",
                                "**Marcação de amistosos (AMIS) não é permitida aqui!**\n\n> 📍 Use os canais de marcação: <#1449070534934401044>, <#1449070508816728198>, <#1449070445327421682> ou <#1457070154226602208>",
                                Color.RED
                        );
                        sendWarningDM(event.getMember(), "Marcação de AMIS em canal incorreto", "Mensagem deletada");
                        logPunishment(event.getGuild(), event.getMember(), "AMIS em canal incorreto", "Mensagem Deletada");
                        return;
                    }
                }
            }

            boolean hasRobloxLink = messageLower.matches(".*(roblox\\.com|ro\\.blox\\.com|rblx\\.co).*");
            boolean hasOtherLink = messageLower.matches(".*(https?://|www\\.|discord\\.gg/)\\S+.*") && !hasRobloxLink;

            if (hasRobloxLink && channelId.equals("1453095863823110184")) {
                return;
            }

            if (hasRobloxLink && channelId.equals(PENEIRA_CHANNEL)) {
                boolean isOlheiro = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(OLHEIRO_ROLE_ID));
                if (isOlheiro) {
                    return;
                } else {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(
                            event.getChannel().asTextChannel(),
                            event.getMember(),
                            "❌ APENAS OLHEIROS",
                            "**Você não tem permissão para enviar links do Roblox aqui!**\n\n> 👁️ Apenas membros com cargo **Olheiro** podem enviar links do Roblox no canal de peneira.\n> 📋 Solicite o cargo com a administração.",
                            Color.RED
                    );
                    sendWarningDM(event.getMember(), "Tentativa de enviar link do Roblox sem cargo de Olheiro", "Mensagem deletada - Apenas Olheiros podem enviar links no canal de peneira");
                    logPunishment(event.getGuild(), event.getMember(), "Link Roblox sem Olheiro", "Mensagem Deletada");
                    return;
                }
            }

            if (hasRobloxLink && channelId.equals(SCRIM_CHANNEL)) {
                boolean isScrimHoster = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(SCRIM_HOSTER_ROLE_ID));
                if (isScrimHoster) {
                    return;
                } else {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(
                            event.getChannel().asTextChannel(),
                            event.getMember(),
                            "❌ APENAS SCRIM HOSTER",
                            "**Você não tem permissão para enviar links do Roblox aqui!**\n\n> 🎯 Apenas membros com cargo **Scrim Hoster** podem enviar links do Roblox no canal de scrim.\n> 📋 Solicite o cargo com a administração.",
                            Color.RED
                    );
                    sendWarningDM(event.getMember(), "Tentativa de enviar link do Roblox sem cargo de Scrim Hoster", "Mensagem deletada - Apenas Scrim Hoster podem enviar links no canal de scrim");
                    logPunishment(event.getGuild(), event.getMember(), "Link Roblox sem Scrim Hoster", "Mensagem Deletada");
                    return;
                }
            }

            if (hasOtherLink && !ALLOWED_LINK_CHANNELS.contains(channelId)) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(
                        event.getChannel().asTextChannel(),
                        event.getMember(),
                        "❌ LINKS NÃO PERMITIDOS",
                        "**Este canal não permite links!**\n\n> 📌 Use os canais autorizados para compartilhar links.\n> 🔗 Canais permitidos: <#1449070133778714738> <#1449112362912186389>",
                        Color.RED
                );
                sendWarningDM(event.getMember(), "Envio de link em canal não autorizado", "Mensagem deletada");
                logPunishment(event.getGuild(), event.getMember(), "Link proibido", "Mensagem Deletada");
                return;
            }

            if (hasRobloxLink && !channelId.equals("1453095863823110184") && !channelId.equals(PENEIRA_CHANNEL) && !channelId.equals(SCRIM_CHANNEL) && !ALLOWED_LINK_CHANNELS.contains(channelId)) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(
                        event.getChannel().asTextChannel(),
                        event.getMember(),
                        "❌ LINKS DO ROBLOX NÃO PERMITIDOS AQUI",
                        "**Use o canal correto para links do Roblox!**\n\n> 📍 Canais permitidos:\n> • <#1453095863823110184> (Todos)\n> • <#" + PENEIRA_CHANNEL + "> (Apenas Olheiros)\n> • <#" + SCRIM_CHANNEL + "> (Apenas Scrim Hoster)",
                        Color.RED
                );
                sendWarningDM(event.getMember(), "Link do Roblox enviado em canal não autorizado", "Mensagem deletada - Use os canais corretos");
                logPunishment(event.getGuild(), event.getMember(), "Link Roblox proibido", "Mensagem Deletada");
                return;
            }

            if (messageLower.contains("f/a") || messageLower.matches(".*\\bf/a\\b.*")) {
                if (!channelId.equals(FA_ALLOWED_CHANNEL)) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(
                            event.getChannel().asTextChannel(),
                            event.getMember(),
                            "❌ F/A NÃO PERMITIDO AQUI",
                            "**Use o canal correto para F/A!**\n\n> 📍 Canal permitido: <#" + FA_ALLOWED_CHANNEL + ">\n> ℹ️ F/A só é permitido no canal específico.",
                            Color.RED
                    );
                    sendWarningDM(event.getMember(), "Uso de 'F/A' em canal incorreto", "Mensagem deletada - Use o canal correto");
                    logPunishment(event.getGuild(), event.getMember(), "F/A em canal errado", "Mensagem Deletada");
                    return;
                }
            }

            if (messageLower.contains("peneira para") || messageLower.contains("peneira pro")) {
                boolean isOlheiro = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(OLHEIRO_ROLE_ID));
                if (!isOlheiro) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(
                            event.getChannel().asTextChannel(),
                            event.getMember(),
                            "❌ APENAS OLHEIROS",
                            "**Você não tem permissão para divulgar peneiras!**\n\n> 👁️ Apenas membros com cargo **Olheiro** podem divulgar peneiras.\n> 📋 Solicite o cargo com a administração.",
                            Color.RED
                    );
                    sendWarningDM(event.getMember(), "Tentativa de divulgar peneira sem cargo de Olheiro", "Mensagem deletada");
                    logPunishment(event.getGuild(), event.getMember(), "Divulgação Peneira s/ Olheiro", "Mensagem Deletada");
                    return;
                }
            }

            for (String keyword : SPAM_KEYWORDS) {
                if (messageLower.contains(keyword)) {
                    event.getMessage().delete().queue();

                    int warnings = userWarnings.getOrDefault(userId + "_spam", 0) + 1;
                    userWarnings.put(userId + "_spam", warnings);

                    if (warnings >= 2) {
                        if (event.getMember() != null) {
                            event.getMember().timeoutFor(Duration.ofMinutes(30)).queue();
                        }
                        sendTemporaryWarning(
                                event.getChannel().asTextChannel(),
                                event.getMember(),
                                "🚫 SILENCIADO POR 30 MINUTOS",
                                "**Spam de anúncios não autorizados! (2º aviso)**\n\n> ⏰ Timeout de **30 minutos** aplicado.\n> 🚫 Não faça propaganda sem autorização da administração.\n> 📌 Palavra detectada: `" + keyword + "`",
                                Color.RED
                        );
                        sendWarningDM(event.getMember(), "Spam de anúncios/serviços (2º aviso) - Palavra: " + keyword, "Timeout de 30 minutos aplicado");
                        logPunishment(event.getGuild(), event.getMember(), "Spam Anúncio (2x)", "Timeout 30m");
                        userWarnings.put(userId + "_spam", 0);
                    } else {
                        sendTemporaryWarning(
                                event.getChannel().asTextChannel(),
                                event.getMember(),
                                "⚠️ AVISO " + warnings + "/2",
                                "**Anúncio não autorizado detectado!**\n\n> 🚨 Próximo aviso resultará em **timeout de 30 minutos**.\n> 📝 Não faça propaganda sem autorização.\n> 📌 Palavra detectada: `" + keyword + "`",
                                Color.ORANGE
                        );
                        sendWarningDM(event.getMember(), "Anúncio não autorizado (" + keyword + ")", "Aviso " + warnings + "/2 - Mensagem deletada");
                        logPunishment(event.getGuild(), event.getMember(), "Spam Anúncio", "Aviso " + warnings + "/2");
                    }
                    return;
                }
            }
        }

        if (messageRaw.startsWith("!") && !isAdmin(event)) {
            if(messageRaw.startsWith("!dmall") || messageRaw.startsWith("!verify") || messageRaw.startsWith("!verifysorteio") || messageRaw.startsWith("!embed")) {
                event.getChannel().sendMessage("❌ Apenas administradores podem usar este comando!").queue();
            }
            return;
        }

        if (messageRaw.equalsIgnoreCase("!verifysorteio")) {
            EmbedBuilder sorteioEmbed = new EmbedBuilder()
                    .setTitle("🎁 SORTEIO DE OLHEIRO GRÁTIS!")
                    .setDescription("""
                            ## 🔥 __ATENÇÃO! SORTEIO ESPECIAL__ 🔥
                            
                            Estamos sorteando **OLHEIRO DE GRAÇA** para membros verificados!
                            
                            > **📋 Como participar:**
                            > 1️⃣ Clique em **"✅ Ir para Verificação"**
                            > 2️⃣ Faça sua verificação
                            > 3️⃣ Clique em **"🎁 Participar do Sorteio"**
                            
                            ⚡ **É RÁPIDO!** Em 10 segundos você está participando!
                            
                            **Premiação:** Olheiro grátis 🎯
                            
                            🍀 __Boa sorte a todos!__
                            """)
                    .setColor(new Color(255, 215, 0))
                    .setThumbnail(CUSTOM_ICON)
                    .setFooter("Pafo • Sorteio Olheiro", CUSTOM_ICON)
                    .setTimestamp(Instant.now());

            ActionRow actionRow = ActionRow.of(
                    Button.link(VERIFY_CHANNEL_LINK, "✅ Ir para Verificação"),
                    Button.primary("go_sorteio", "🎁 Participar do Sorteio")
            );

            sendDMToAll(event, null, sorteioEmbed.build(), actionRow);
            return;
        }

        if (messageRaw.equalsIgnoreCase("!verify")) {
            TextChannel verifyChannel = event.getGuild().getTextChannelById(VERIFY_CHANNEL_ID);

            if (verifyChannel == null) {
                event.getChannel().sendMessage("❌ Canal não encontrado! ID incorreto.").queue();
                return;
            }

            EmbedBuilder verifyEmbed = new EmbedBuilder()
                    .setTitle("🔐 Verificação do Servidor")
                    .setDescription("""
                            **Bem-vindo(a) ao Pafo!** 👋
                            
                            > Para ter acesso completo, você precisa se verificar.
                            
                            **📋 Como verificar:**
                            1. Clique no botão **"✅ Verificar"**
                            2. Digite seu nome de usuário do Roblox
                            3. Pronto! Acesso liberado! 🎉
                            
                            **Seu apelido ficará:** SeuNome (@RobloxUser)
                            
                            ⚡ É rápido e fácil!
                            """)
                    .setColor(new Color(87, 242, 135))
                    .setThumbnail(CUSTOM_ICON)
                    .setFooter("Sistema de Verificação • PafoVerify", CUSTOM_ICON)
                    .setTimestamp(Instant.now());

            verifyChannel.sendMessageEmbeds(verifyEmbed.build())
                    .setActionRow(Button.success("verify_button", "✅ Verificar"))
                    .queue(
                            success -> event.getChannel().sendMessage("✅ Embed de verificação enviada com sucesso!").queue(),
                            error -> event.getChannel().sendMessage("❌ Erro ao enviar embed! Verifique permissões.").queue()
                    );
            return;
        }

        if (messageRaw.startsWith("!dmall ")) {
            String messageToSend = messageRaw.substring(7);
            sendDMToAll(event, messageToSend, null, null);
            return;
        }

        if (messageRaw.equalsIgnoreCase("!embed")) {
            pendingEmbeds.put(userId, new EmbedData());
            event.getChannel().sendMessage("📝 **Criador de Embed iniciado!** Use `!titulo`, `!desc`, `!cor`, `!enviar`.").queue();
            return;
        }

        if (pendingEmbeds.containsKey(userId)) {
            EmbedData data = pendingEmbeds.get(userId);

            if (messageRaw.startsWith("!titulo ")) {
                data.title = messageRaw.substring(8);
                event.getChannel().sendMessage("✅ Título definido.").queue();
                return;
            } else if (messageRaw.startsWith("!desc ")) {
                data.description = messageRaw.substring(6);
                event.getChannel().sendMessage("✅ Descrição definida.").queue();
                return;
            } else if (messageRaw.startsWith("!cor ")) {
                data.color = getColor(messageRaw.substring(5).trim());
                event.getChannel().sendMessage("✅ Cor definida.").queue();
                return;
            } else if (messageRaw.startsWith("!rodape ")) {
                data.footer = messageRaw.substring(8);
                event.getChannel().sendMessage("✅ Rodapé definido.").queue();
                return;
            } else if (messageRaw.equalsIgnoreCase("!enviar")) {
                if (data.title == null || data.description == null) {
                    event.getChannel().sendMessage("❌ Título e Descrição são obrigatórios!").queue();
                    return;
                }
                MessageEmbed embed = buildEmbed(data);
                data.embed = embed;
                event.getChannel().sendMessage("📋 **Preview:**").queue();
                event.getChannel().sendMessageEmbeds(embed).queue(s -> event.getChannel().sendMessage("Digite `!confirmar` para enviar a todos.").queue());
                return;
            } else if (messageRaw.equalsIgnoreCase("!confirmar")) {
                if (data.embed != null) {
                    sendDMToAll(event, null, data.embed, null);
                    pendingEmbeds.remove(userId);
                } else {
                    event.getChannel().sendMessage("❌ Use `!enviar` primeiro para gerar a embed.").queue();
                }
                return;
            } else if (messageRaw.equalsIgnoreCase("!cancelar")) {
                pendingEmbeds.remove(userId);
                event.getChannel().sendMessage("❌ Cancelado.").queue();
                return;
            }
        }

        if (messageRaw.startsWith("!quickembed")) {
            String[] parts = messageRaw.split("\\|");
            if (parts.length >= 3) {
                String title = parts[1].trim();
                String description = parts[2].trim();
                String colorName = parts.length > 3 ? parts[3].trim() : "azul";

                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(getColor(colorName))
                        .setTimestamp(Instant.now());

                sendDMToAll(event, null, builder.build(), null);
            } else {
                event.getChannel().sendMessage("❌ Uso: `!quickembed | Título | Desc | Cor`").queue();
            }
        }
    }

    private MessageEmbed buildEmbed(EmbedData data) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(data.title)
                .setDescription(data.description)
                .setColor(data.color != null ? data.color : Color.BLUE)
                .setTimestamp(Instant.now());
        if (data.footer != null) builder.setFooter(data.footer);
        return builder.build();
    }

    private Color getColor(String colorName) {
        String lower = colorName.toLowerCase();
        return switch (lower) {
            case "vermelho", "red" -> Color.RED;
            case "verde", "green" -> Color.GREEN;
            case "amarelo", "yellow" -> Color.YELLOW;
            case "roxo", "purple" -> new Color(128, 0, 128);
            case "preto", "black" -> Color.BLACK;
            case "branco", "white" -> Color.WHITE;
            case "laranja", "orange" -> Color.ORANGE;
            default -> Color.BLUE;
        };
    }

    private void sendDMToAll(MessageReceivedEvent event, String textMessage, MessageEmbed embed, ActionRow actionRow) {
        event.getGuild().loadMembers().onSuccess(members -> {
            List<Member> targetMembers = members.stream()
                    .filter(m -> !m.getUser().isBot())
                    .toList();

            int totalMembers = targetMembers.size();
            event.getChannel().sendMessage(String.format("🚀 Iniciando envio para **%d** membros... (Delay: 1.5s)", totalMembers)).queue();

            AtomicInteger sentCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            AtomicInteger processedCount = new AtomicInteger(0);

            for (int i = 0; i < targetMembers.size(); i++) {
                Member member = targetMembers.get(i);
                long delay = (long) i * DELAY_PER_MESSAGE_MS;

                scheduler.schedule(() -> {
                    member.getUser().openPrivateChannel().queue(
                            privateChannel -> {
                                if (embed != null) {
                                    var action = privateChannel.sendMessageEmbeds(embed);
                                    if (actionRow != null) {
                                        action = action.setComponents(actionRow);
                                    }
                                    action.queue(
                                            success -> {
                                                sentCount.incrementAndGet();
                                                processedCount.incrementAndGet();
                                                logger.info("Enviado para: {}", member.getUser().getName());
                                            },
                                            error -> {
                                                failCount.incrementAndGet();
                                                processedCount.incrementAndGet();
                                                logger.warn("Falha (DM Fechada/Bloqueada): {}", member.getUser().getName());
                                            }
                                    );
                                } else if (textMessage != null) {
                                    var action = privateChannel.sendMessage(textMessage);
                                    if (actionRow != null) {
                                        action = action.setComponents(actionRow);
                                    }
                                    action.queue(
                                            success -> {
                                                sentCount.incrementAndGet();
                                                processedCount.incrementAndGet();
                                                logger.info("Enviado para: {}", member.getUser().getName());
                                            },
                                            error -> {
                                                failCount.incrementAndGet();
                                                processedCount.incrementAndGet();
                                                logger.warn("Falha (DM Fechada/Bloqueada): {}", member.getUser().getName());
                                            }
                                    );
                                }
                            },
                            error -> {
                                failCount.incrementAndGet();
                                processedCount.incrementAndGet();
                                logger.warn("Falha ao abrir canal com: {}", member.getUser().getName());
                            }
                    );
                }, delay, TimeUnit.MILLISECONDS);
            }

            long reportDelay = (targetMembers.size() * DELAY_PER_MESSAGE_MS) + 5000;

            scheduler.schedule(() -> {
                String report = String.format("""
                        📊 **Relatório Final:**
                        ✅ Sucessos: %d
                        ❌ Falhas (DM Fechada): %d
                        📋 Total Processado: %d / %d
                        """, sentCount.get(), failCount.get(), processedCount.get(), totalMembers);
                event.getChannel().sendMessage(report).queue();
            }, reportDelay, TimeUnit.MILLISECONDS);

        }).onError(error -> {
            event.getChannel().sendMessage("❌ Erro fatal ao carregar lista de membros!").queue();
            logger.error("Erro loadMembers: ", error);
        });
    }

    private static class EmbedData {
        String title;
        String description;
        Color color;
        String footer;
        MessageEmbed embed;
    }
}