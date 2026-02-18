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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, EmbedData> pendingEmbeds = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> userMessageTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Integer> userWarnings = new ConcurrentHashMap<>();

    // ─── IDs DO SEU SERVIDOR ───────────────────────────────────────────────────
    private static final String GUILD_ID             = "1449061779060687063";
    private static final String VERIFY_CHANNEL_ID    = "1464627654744477819";
    private static final String VERIFIED_ROLE_ID     = "1464623361626734637";
    private static final String OLHEIRO_ROLE_ID      = "1449070067131224268";
    private static final String SCRIM_HOSTER_ROLE_ID = "1449070133040517262";
    private static final String FA_ALLOWED_CHANNEL   = "1461773344620941534";
    private static final String PENEIRA_CHANNEL      = "1449069785601282068";
    private static final String SCRIM_CHANNEL        = "1450548729303011379";
    private static final String LOG_CHANNEL_ID       = "1449525327175880865";
    private static final String GFX_CHANNEL_ID       = "1461773344620941534";

    private static final List<String> AMIS_CHANNELS = Arrays.asList(
            "1449070534934401044",
            "1449070508816728198",
            "1449070445327421682",
            "1457070154226602208",
            "1457108663603953736",
            "1457070154226602208"
    );

    private static final String SORTEIO_CANAL_LINK  = "https://discord.com/channels/1449061779060687063/1449115997804957806";
    private static final String VERIFY_CHANNEL_LINK = "https://discord.com/channels/" + GUILD_ID + "/" + VERIFY_CHANNEL_ID;
    private static final String CUSTOM_ICON         = "https://images-ext-1.discordapp.net/external/JqpzlTlIxaM5Yl53AY7hH09Tl8DK5k9lMwwt7oKHIhw/%3Fformat%3Dwebp%26quality%3Dlossless/https/images-ext-1.discordapp.net/external/_7vAzr-RdzvNO08YWoiLiFqxr_3rAymqm_V_R1-jiT4/%253Fsize%253D2048/https/cdn.discordapp.com/icons/1449061779060687063/ecbd3ce76f39128b1ec08154e7faff75.png?format=webp&quality=lossless";

    private static final List<String> ALLOWED_LINK_CHANNELS = Arrays.asList(
            "1449070133778714738", "1449112362912186389", "1453095863823110184", "1449071892873871522"
    );

    private static final List<String> GFX_KEYWORDS = Arrays.asList(
            "gfx gratis", "gfx gratuito", "faço gfx", "fazer gfx", "faça gfx",
            "gfx pago", "vendo gfx", "compro gfx", "preciso de gfx", "quer gfx", "gfx"
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
            "falta", "membros pra", "criar logo", "logo de league", "logo gratis",
            "logo de graça", "técnico de league", "procuro técnico", "busco técnico"
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
            "bucetão", "rabo", "bunda", "peito", "teta", "mama"
    );

    private static final long DELAY_PER_MESSAGE_MS = 1500;

    // ─── SISTEMA DE FILA DE AMISTOSOS ─────────────────────────────────────────
    private final Map<String, TeamData> teams = new ConcurrentHashMap<>();
    private final Map<String, List<QueueEntry>> queues = new ConcurrentHashMap<>();

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        String token = System.getenv("TOKEN");

        if (token == null) {
            System.out.println("TOKEN ESTA NULL!!!");
        } else {
            System.out.println("TOKEN OK");
        }

        try {
            var jda = JDABuilder.createDefault(token)
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

            jda.awaitReady();
            logger.info("Bot iniciado com sucesso!");

            // Registrar slash commands
            jda.updateCommands().addCommands(

                    Commands.slash("registrar-time", "Registra seu time para amistosos")
                            .addOptions(
                                    new OptionData(OptionType.STRING, "nome", "Nome do seu time", true),
                                    new OptionData(OptionType.BOOLEAN, "seu-host", "Você tem servidor privado (host próprio)?", true),
                                    new OptionData(OptionType.STRING, "link-servidor", "Link do seu servidor privado (se tiver)", false)
                            ),

                    Commands.slash("fila", "Entrar na fila de amistosos")
                            .addOptions(
                                    new OptionData(OptionType.STRING, "modo", "Modo de jogo", true)
                                            .addChoice("5v5", "5v5")
                                            .addChoice("6v6", "6v6")
                                            .addChoice("7v7", "7v7")
                                            .addChoice("4v4", "4v4"),
                                    new OptionData(OptionType.BOOLEAN, "seu-host", "Você será o host do servidor?", true)
                            ),

                    Commands.slash("sair-fila", "Sair da fila de amistosos"),

                    Commands.slash("fila-status", "Ver quem está na fila"),

                    Commands.slash("agendar", "Agendar um amistoso para um horário")
                            .addOptions(
                                    new OptionData(OptionType.STRING, "modo", "Modo de jogo", true)
                                            .addChoice("5v5", "5v5")
                                            .addChoice("6v6", "6v6")
                                            .addChoice("7v7", "7v7")
                                            .addChoice("4v4", "4v4"),
                                    new OptionData(OptionType.STRING, "horario", "Horário (ex: 19:30)", true)
                            )

            ).queue();

            logger.info("Slash commands registrados!");

        } catch (Exception e) {
            logger.error("Erro ao iniciar o bot", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SLASH COMMANDS
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "registrar-time" -> handleRegistrar(event);
            case "fila"           -> handleFila(event);
            case "sair-fila"      -> handleSairFila(event);
            case "fila-status"    -> handleFilaStatus(event);
            case "agendar"        -> handleAgendar(event);
        }
    }

    private void handleRegistrar(SlashCommandInteractionEvent event) {
        String userId   = event.getUser().getId();
        String nome     = event.getOption("nome", OptionMapping::getAsString);
        boolean temHost = Boolean.TRUE.equals(event.getOption("seu-host", OptionMapping::getAsBoolean));
        String link     = event.getOption("link-servidor", OptionMapping::getAsString);

        if (nome == null || nome.isBlank()) {
            event.replyEmbeds(embedErro("Nome inválido", "Digite um nome para o seu time.").build())
                    .setEphemeral(true).queue();
            return;
        }

        // Se tem host mas não passou link agora, tenta reaproveitar o link anterior
        TeamData anterior = teams.get(userId);
        if (temHost && (link == null || link.isBlank())) {
            if (anterior != null && anterior.link != null && !anterior.link.isBlank()) {
                link = anterior.link; // reutiliza o link já registrado
            } else {
                event.replyEmbeds(embedErro("Link necessário",
                                "Você marcou que tem host próprio. Informe o link do seu servidor privado.").build())
                        .setEphemeral(true).queue();
                return;
            }
        }

        boolean estaAtualizando = anterior != null;
        teams.put(userId, new TeamData(nome, temHost, link));

        String titulo = estaAtualizando ? "Time Atualizado!" : "Time Registrado!";
        String desc   = estaAtualizando
                ? "Seu time foi atualizado com sucesso!\n> ⚠️ Se você estava na fila, saia e entre novamente para aplicar as mudanças."
                : "";

        EmbedBuilder embed = embedSucesso(titulo, desc)
                .addField("🏆 Time", nome, true)
                .addField("🏠 Host", temHost ? "Sim ✅" : "Não ❌", true)
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Use /fila para entrar na fila de amistosos!", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        if (temHost && link != null) embed.addField("🔗 Servidor", link, false);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void handleFila(SlashCommandInteractionEvent event) {
        String userId  = event.getUser().getId();
        String modo    = event.getOption("modo", OptionMapping::getAsString);
        boolean isHost = Boolean.TRUE.equals(event.getOption("seu-host", OptionMapping::getAsBoolean));

        if (!teams.containsKey(userId)) {
            event.replyEmbeds(embedErro("Time não registrado",
                            "Registre seu time primeiro com `/registrar-time`!").build())
                    .setEphemeral(true).queue();
            return;
        }

        if (isInQueue(userId)) {
            String modoAtual = getQueueMode(userId);
            event.replyEmbeds(embedErro("Já na fila",
                            "Você já está na fila de **" + modoAtual + "**!\nUse `/sair-fila` para sair.").build())
                    .setEphemeral(true).queue();
            return;
        }

        TeamData time = teams.get(userId);

        // Se quer ser host mas não tem link registrado, avisa mas deixa entrar
        // (pode ter server privado disponível só naquele momento)
        String avisoHost = "";
        if (isHost && (time.link == null || time.link.isBlank())) {
            avisoHost = "\n\n⚠️ Você não tem link de servidor registrado.\nQuando o match acontecer, entre em contato com o adversário no privado para combinar o servidor.";
        }

        QueueEntry entry = new QueueEntry(userId, event.getUser().getAsTag(), modo, isHost);
        queues.computeIfAbsent(modo, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);

        EmbedBuilder confirmEmbed = new EmbedBuilder()
                .setTitle("🔍 Na fila de " + modo + "!")
                .setDescription("Aguardando adversário...\nUse `/sair-fila` para cancelar." + avisoHost)
                .addField("🏆 Seu time", time.nome, true)
                .addField("🏠 Host", isHost ? "Sim ✅" : "Não ❌", true)
                .setColor(new Color(0x9B59B6))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • Pafo", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        event.replyEmbeds(confirmEmbed.build()).setEphemeral(true).queue();

        // Tentar match
        QueueEntry[] match = tryMatch(modo);
        if (match != null) processMatch(event, match[0], match[1], modo);
    }

    private void processMatch(SlashCommandInteractionEvent event, QueueEntry p1, QueueEntry p2, String modo) {
        TeamData t1 = teams.get(p1.userId);
        TeamData t2 = teams.get(p2.userId);

        // Quem é host nesse match específico (decidido na hora do /fila)
        QueueEntry hostEntry = p1.isHost ? p1 : p2;
        QueueEntry guestEntry = p1.isHost ? p2 : p1;
        TeamData   hostTeam  = teams.get(hostEntry.userId);

        // Link pode ser o registrado no time, ou combinado no pv se não tiver
        String hostInfo;
        if (hostTeam != null && hostTeam.link != null && !hostTeam.link.isBlank()) {
            hostInfo = "\n🔗 **Link do servidor:** " + hostTeam.link;
        } else {
            hostInfo = "\n*(Sem link registrado — o host " + (hostTeam != null ? "**" + hostTeam.nome + "**" : hostEntry.username) + " vai te contatar no privado!)*";
        }

        String nomeT1 = t1 != null ? t1.nome : p1.username;
        String nomeT2 = t2 != null ? t2.nome : p2.username;
        String nomeHost = hostTeam != null ? hostTeam.nome : hostEntry.username;

        String descHost = """
                ## ⚽ MATCH ENCONTRADO!
                
                🏆 **%s** vs **%s**
                🎮 Modo: `%s`
                🏠 Host: **%s**
                %s
                
                👉 O guest deve entrar em contato com o host no privado!
                """.formatted(nomeT1, nomeT2, modo, nomeHost, hostInfo);

        String descGuest = """
                ## ⚽ MATCH ENCONTRADO!
                
                🏆 **%s** vs **%s**
                🎮 Modo: `%s`
                🏠 Host: **%s**
                %s
                
                👉 Entre em contato com o host: <@%s>
                """.formatted(nomeT1, nomeT2, modo, nomeHost, hostInfo, hostEntry.userId);

        EmbedBuilder embedHost = new EmbedBuilder()
                .setTitle("⚽ Match Encontrado!")
                .setDescription(descHost)
                .setColor(new Color(0xF1C40F))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • Pafo", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        EmbedBuilder embedGuest = new EmbedBuilder()
                .setTitle("⚽ Match Encontrado!")
                .setDescription(descGuest)
                .setColor(new Color(0xF1C40F))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • Pafo", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        // Host recebe info do adversário
        notifyUserDM(event, hostEntry.userId, embedHost.build());
        // Guest recebe info de quem é o host com menção
        notifyUserDM(event, guestEntry.userId, embedGuest.build());

        logger.info("MATCH: {} vs {} no modo {}", p1.username, p2.username, modo);
    }

    private void notifyUserDM(SlashCommandInteractionEvent event, String userId, MessageEmbed embed) {
        event.getJDA().retrieveUserById(userId).queue(user ->
                user.openPrivateChannel().queue(ch ->
                        ch.sendMessageEmbeds(embed).queue(
                                ok  -> logger.info("DM enviada para {}", user.getAsTag()),
                                err -> logger.warn("Não foi possível enviar DM para {}", user.getAsTag())
                        )
                )
        );
    }

    private void handleSairFila(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        boolean removed = queues.values().stream()
                .anyMatch(list -> list.removeIf(e -> e.userId.equals(userId)));

        if (removed) {
            event.replyEmbeds(embedSucesso("Saiu da fila", "Você saiu com sucesso!").build())
                    .setEphemeral(true).queue();
        } else {
            event.replyEmbeds(embedErro("Não estava na fila", "Você não está em nenhuma fila.").build())
                    .setEphemeral(true).queue();
        }
    }

    private void handleFilaStatus(SlashCommandInteractionEvent event) {
        boolean vazia = queues.values().stream().allMatch(List::isEmpty);

        if (vazia) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("🔍 Fila vazia")
                            .setDescription("Nenhum jogador na fila. Use `/fila` para entrar!")
                            .setColor(new Color(0x3498DB)).build())
                    .setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔍 Status das Filas")
                .setColor(new Color(0x9B59B6))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("🏠 = Host | 🎮 = Sem host", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        for (String modo : List.of("5v5", "6v6", "7v7", "4v4")) {
            List<QueueEntry> lista = queues.getOrDefault(modo, Collections.emptyList());
            if (!lista.isEmpty()) {
                StringJoiner sb = new StringJoiner("\n");
                for (QueueEntry e : lista) {
                    TeamData t = teams.get(e.userId);
                    String nome = t != null ? t.nome : e.username;
                    sb.add("• **" + nome + "** " + (e.isHost ? "🏠" : "🎮"));
                }
                embed.addField("⚽ " + modo + " (" + lista.size() + " na fila)", sb.toString(), false);
            }
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void handleAgendar(SlashCommandInteractionEvent event) {
        String userId  = event.getUser().getId();
        String modo    = event.getOption("modo", OptionMapping::getAsString);
        String horario = event.getOption("horario", OptionMapping::getAsString);

        if (!teams.containsKey(userId)) {
            event.replyEmbeds(embedErro("Time não registrado",
                            "Registre seu time primeiro com `/registrar-time`!").build())
                    .setEphemeral(true).queue();
            return;
        }

        if (horario == null || !horario.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            event.replyEmbeds(embedErro("Horário inválido", "Use o formato `HH:MM`. Ex: `19:30`").build())
                    .setEphemeral(true).queue();
            return;
        }

        TeamData time = teams.get(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📅 Amistoso Agendado!")
                .setDescription("""
                        <@%s> está procurando adversário para amistoso!
                        
                        📌 Entre em contato no privado para confirmar.
                        """.formatted(userId))
                .addField("🏆 Time", time.nome, true)
                .addField("🎮 Modo", modo, true)
                .addField("🕐 Horário", horario, true)
                .addField("🏠 Host", time.temHost ? "Sim ✅" : "Não ❌", true)
                .setColor(new Color(0xF1C40F))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • Pafo", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        if (time.temHost && time.link != null) embed.addField("🔗 Servidor", time.link, false);

        event.replyEmbeds(embed.build()).queue(); // público, não ephemeral
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helpers de fila
    // ──────────────────────────────────────────────────────────────────────────

    private boolean isInQueue(String userId) {
        return queues.values().stream().anyMatch(l -> l.stream().anyMatch(e -> e.userId.equals(userId)));
    }

    private String getQueueMode(String userId) {
        for (var entry : queues.entrySet())
            if (entry.getValue().stream().anyMatch(e -> e.userId.equals(userId)))
                return entry.getKey();
        return null;
    }

    private QueueEntry[] tryMatch(String modo) {
        List<QueueEntry> list = queues.getOrDefault(modo, Collections.emptyList());
        synchronized (list) {
            if (list.size() >= 2) {
                QueueEntry host  = list.stream().filter(e -> e.isHost).findFirst().orElse(null);
                QueueEntry guest = list.stream().filter(e -> !e.isHost).findFirst().orElse(null);

                QueueEntry p1, p2;
                if (host != null && guest != null && !host.userId.equals(guest.userId)) {
                    p1 = host; p2 = guest;
                } else {
                    p1 = list.get(0); p2 = list.get(1);
                    if (p1.userId.equals(p2.userId)) return null;
                }
                list.remove(p1); list.remove(p2);
                return new QueueEntry[]{p1, p2};
            }
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AUTOMOD
    // ══════════════════════════════════════════════════════════════════════════

    private boolean isAdmin(MessageReceivedEvent event) {
        if (event.getMember() == null) return false;
        return event.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    private void sendWarningDM(Member member, String reason, String action) {
        member.getUser().openPrivateChannel().queue(
                ch -> {
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
                    ch.sendMessageEmbeds(embed.build()).queue(
                            ok  -> logger.info("DM de aviso enviada para: {}", member.getUser().getName()),
                            err -> logger.warn("Não foi possível enviar DM para: {}", member.getUser().getName())
                    );
                },
                err -> logger.warn("Não foi possível abrir canal privado com: {}", member.getUser().getName())
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

        channel.sendMessageEmbeds(embed.build()).queue(msg ->
                scheduler.schedule(() -> msg.delete().queue(
                        ok  -> logger.info("Embed de aviso deletado"),
                        err -> logger.warn("Erro ao deletar embed de aviso")
                ), 60, TimeUnit.SECONDS)
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODAL & BOTÃO
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("roblox_modal")) return;

        var input = event.getValue("roblox_username");
        String robloxUsername = (input != null) ? input.getAsString() : "Desconhecido";
        Member member = event.getMember();

        if (member == null) { event.reply("❌ Erro: Membro não encontrado.").setEphemeral(true).queue(); return; }

        String currentNick = member.getEffectiveName();
        String newNick = currentNick + " (@" + robloxUsername + ")";
        if (newNick.length() > 32) newNick = newNick.substring(0, 32);

        String finalNick = newNick;
        member.modifyNickname(finalNick).queue(
                ok  -> addRoleAndReply(event, member, finalNick, true),
                err -> addRoleAndReply(event, member, finalNick, false)
        );
    }

    private void addRoleAndReply(ModalInteractionEvent event, Member member, String newNick, boolean nickChanged) {
        Role verifiedRole = event.getGuild().getRoleById(VERIFIED_ROLE_ID);
        if (verifiedRole != null) event.getGuild().addRoleToMember(member, verifiedRole).queue();

        if (nickChanged) {
            event.reply("✅ **VERIFICADO COM SUCESSO!** 🎉\n\n**Seu novo apelido:** %s\n\nVocê agora tem acesso total ao servidor! 🚀".formatted(newNick))
                    .setEphemeral(true).queue();
        } else {
            event.reply("✅ **VERIFICADO!** 🎉\n\n(Não consegui alterar seu apelido.)\nVocê pode alterar manualmente para: %s".formatted(newNick))
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();

        if (id.equals("verify_button")) {
            Member member = event.getMember();
            if (member == null) { event.reply("❌ Erro ao verificar membro.").setEphemeral(true).queue(); return; }

            Role verifiedRole = event.getGuild().getRoleById(VERIFIED_ROLE_ID);
            if (verifiedRole != null && member.getRoles().contains(verifiedRole)) {
                event.reply("✅ Você já está verificado!").setEphemeral(true).queue();
                return;
            }

            TextInput robloxInput = TextInput.create("roblox_username", "Nome de usuário do Roblox", TextInputStyle.SHORT)
                    .setPlaceholder("Digite seu usuário do Roblox")
                    .setRequired(true).setMaxLength(20).build();

            Modal modal = Modal.create("roblox_modal", "🎮 Verificação Roblox")
                    .addActionRow(robloxInput).build();

            event.replyModal(modal).queue();

        } else if (id.equals("go_sorteio")) {
            event.reply("🎁 **Link do Sorteio:**\n" + SORTEIO_CANAL_LINK + "\n\nBoa sorte! 🍀").setEphemeral(true).queue();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENSAGENS — automod + comandos !
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String messageRaw  = event.getMessage().getContentRaw();
        String messageLower = messageRaw.toLowerCase();
        String userId      = event.getAuthor().getId();
        String channelId   = event.getChannel().getId();

        if (!isAdmin(event)) {

            // Anti-spam de velocidade
            long now = System.currentTimeMillis();
            userMessageTimestamps.putIfAbsent(userId, new ArrayList<>());
            List<Long> times = userMessageTimestamps.get(userId);
            times.add(now);
            times.removeIf(t -> now - t > 5000);

            if (times.size() >= 5) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                        "⚠️ SPAM DETECTADO",
                        "**Pare de enviar mensagens tão rápido!**\n\n> Você enviou 5+ mensagens em 5 segundos.\n> Aguarde antes de enviar novamente.",
                        Color.ORANGE);
                sendWarningDM(event.getMember(), "Spam de mensagens (5+ em 5s)", "Mensagem deletada + Aviso");
                logPunishment(event.getGuild(), event.getMember(), "Spam (5+ msg em 5s)", "Mensagem Deletada + Aviso");
                return;
            }

            // Palavrões
            for (String word : PROFANITY) {
                if (messageLower.matches(".*\\b" + word + "\\b.*")) {
                    event.getMessage().delete().queue();
                    int warnings = userWarnings.getOrDefault(userId, 0) + 1;
                    userWarnings.put(userId, warnings);

                    if (warnings >= 3) {
                        if (event.getMember() != null) event.getMember().timeoutFor(Duration.ofHours(1)).queue();
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "🚫 SILENCIADO POR 1 HORA",
                                "**Você atingiu 3 avisos por linguagem inadequada!**\n\n> ⏰ Timeout de **1 hora** aplicado.\n> 📖 Leia as regras do servidor.",
                                Color.RED);
                        sendWarningDM(event.getMember(), "Linguagem inadequada (3º aviso)", "Timeout de 1 hora");
                        logPunishment(event.getGuild(), event.getMember(), "Linguagem inadequada (3x)", "Timeout 1h");
                        userWarnings.put(userId, 0);
                    } else {
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "⚠️ AVISO " + warnings + "/3",
                                "**Linguagem inadequada detectada!**\n\n> 🚨 No 3º aviso você leva timeout de **1 hora**.\n> 📝 Evite usar palavrões.",
                                Color.ORANGE);
                        sendWarningDM(event.getMember(), "Linguagem inadequada", "Aviso " + warnings + "/3 - Mensagem deletada");
                        logPunishment(event.getGuild(), event.getMember(), "Linguagem inadequada", "Aviso " + warnings + "/3");
                    }
                    return;
                }
            }

            // GFX no canal errado
            for (String gfxKey : GFX_KEYWORDS) {
                if (messageLower.contains(gfxKey) && !channelId.equals(GFX_CHANNEL_ID)) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ CANAL INCORRETO DE GFX",
                            "**Pedidos ou vendas de GFX não são permitidos aqui!**\n\n> 📍 Use o canal correto: <#" + GFX_CHANNEL_ID + ">",
                            Color.RED);
                    sendWarningDM(event.getMember(), "GFX em canal incorreto", "Mensagem deletada");
                    logPunishment(event.getGuild(), event.getMember(), "GFX em canal incorreto", "Mensagem Deletada");
                    return;
                }
            }

            // AMIS no canal errado
            for (String amisKey : AMIS_KEYWORDS) {
                if (messageLower.contains(amisKey)) {
                    if (channelId.equals(FA_ALLOWED_CHANNEL)) continue;
                    if (!AMIS_CHANNELS.contains(channelId)) {
                        event.getMessage().delete().queue();
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "❌ CANAL INCORRETO DE AMIS",
                                "**Marcação de amistosos não é permitida aqui!**\n\n> 📍 Use: <#1449070534934401044>, <#1449070508816728198>, <#1449070445327421682> ou <#1457070154226602208>",
                                Color.RED);
                        sendWarningDM(event.getMember(), "AMIS em canal incorreto", "Mensagem deletada");
                        logPunishment(event.getGuild(), event.getMember(), "AMIS em canal incorreto", "Mensagem Deletada");
                        return;
                    }
                }
            }

            boolean hasRobloxLink = messageLower.matches(".*(roblox\\.com|ro\\.blox\\.com|rblx\\.co).*");
            boolean hasOtherLink  = messageLower.matches(".*(https?://|www\\.|discord\\.gg/)\\S+.*") && !hasRobloxLink;

            if (hasRobloxLink && channelId.equals("1453095863823110184")) return;

            if (hasRobloxLink && channelId.equals(PENEIRA_CHANNEL)) {
                boolean isOlheiro = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(OLHEIRO_ROLE_ID));
                if (!isOlheiro) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ APENAS OLHEIROS",
                            "**Você não tem permissão para enviar links do Roblox aqui!**\n\n> 👁️ Apenas **Olheiros** podem enviar links no canal de peneira.",
                            Color.RED);
                    sendWarningDM(event.getMember(), "Link Roblox sem cargo Olheiro", "Mensagem deletada");
                    logPunishment(event.getGuild(), event.getMember(), "Link Roblox sem Olheiro", "Mensagem Deletada");
                }
                return;
            }

            if (hasRobloxLink && channelId.equals(SCRIM_CHANNEL)) {
                boolean isScrimHoster = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(SCRIM_HOSTER_ROLE_ID));
                if (!isScrimHoster) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ APENAS SCRIM HOSTER",
                            "**Você não tem permissão para enviar links do Roblox aqui!**\n\n> 🎯 Apenas **Scrim Hoster** podem enviar links no canal de scrim.",
                            Color.RED);
                    sendWarningDM(event.getMember(), "Link Roblox sem cargo Scrim Hoster", "Mensagem deletada");
                    logPunishment(event.getGuild(), event.getMember(), "Link Roblox sem Scrim Hoster", "Mensagem Deletada");
                }
                return;
            }

            if (hasOtherLink && !ALLOWED_LINK_CHANNELS.contains(channelId)) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                        "❌ LINKS NÃO PERMITIDOS",
                        "**Este canal não permite links!**\n\n> 📌 Use os canais autorizados.\n> 🔗 Permitidos: <#1449070133778714738> <#1449112362912186389>",
                        Color.RED);
                sendWarningDM(event.getMember(), "Link em canal não autorizado", "Mensagem deletada");
                logPunishment(event.getGuild(), event.getMember(), "Link proibido", "Mensagem Deletada");
                return;
            }

            if (hasRobloxLink && !channelId.equals("1453095863823110184")
                    && !channelId.equals(PENEIRA_CHANNEL) && !channelId.equals(SCRIM_CHANNEL)
                    && !ALLOWED_LINK_CHANNELS.contains(channelId)) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                        "❌ LINKS DO ROBLOX NÃO PERMITIDOS AQUI",
                        "**Use o canal correto para links do Roblox!**\n\n> 📍 Canais permitidos:\n> • <#1453095863823110184> (Todos)\n> • <#" + PENEIRA_CHANNEL + "> (Apenas Olheiros)\n> • <#" + SCRIM_CHANNEL + "> (Apenas Scrim Hoster)",
                        Color.RED);
                sendWarningDM(event.getMember(), "Link Roblox em canal não autorizado", "Mensagem deletada");
                logPunishment(event.getGuild(), event.getMember(), "Link Roblox proibido", "Mensagem Deletada");
                return;
            }

            if (messageLower.contains("f/a") || messageLower.matches(".*\\bf/a\\b.*")) {
                if (!channelId.equals(FA_ALLOWED_CHANNEL)) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ F/A NÃO PERMITIDO AQUI",
                            "**Use o canal correto para F/A!**\n\n> 📍 Canal permitido: <#" + FA_ALLOWED_CHANNEL + ">\n> ℹ️ F/A só é permitido no canal específico.",
                            Color.RED);
                    sendWarningDM(event.getMember(), "F/A em canal incorreto", "Mensagem deletada");
                    logPunishment(event.getGuild(), event.getMember(), "F/A em canal errado", "Mensagem Deletada");
                    return;
                }
            }

            if (messageLower.contains("peneira para") || messageLower.contains("peneira pro")) {
                boolean isOlheiro = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(OLHEIRO_ROLE_ID));
                if (!isOlheiro) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ APENAS OLHEIROS",
                            "**Você não tem permissão para divulgar peneiras!**\n\n> 👁️ Apenas **Olheiros** podem divulgar peneiras.",
                            Color.RED);
                    sendWarningDM(event.getMember(), "Peneira sem cargo Olheiro", "Mensagem deletada");
                    logPunishment(event.getGuild(), event.getMember(), "Divulgação Peneira s/ Olheiro", "Mensagem Deletada");
                    return;
                }
            }

            // Spam de anúncios
            for (String keyword : SPAM_KEYWORDS) {
                if (messageLower.contains(keyword)) {
                    event.getMessage().delete().queue();
                    int warnings = userWarnings.getOrDefault(userId + "_spam", 0) + 1;
                    userWarnings.put(userId + "_spam", warnings);

                    if (warnings >= 2) {
                        if (event.getMember() != null) event.getMember().timeoutFor(Duration.ofMinutes(30)).queue();
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "🚫 SILENCIADO POR 30 MINUTOS",
                                "**Spam de anúncios não autorizados! (2º aviso)**\n\n> ⏰ Timeout de **30 minutos** aplicado.\n> 🚫 Não faça propaganda sem autorização.\n> 📌 Palavra detectada: `" + keyword + "`",
                                Color.RED);
                        sendWarningDM(event.getMember(), "Spam anúncio (2x) - " + keyword, "Timeout 30 minutos");
                        logPunishment(event.getGuild(), event.getMember(), "Spam Anúncio (2x)", "Timeout 30m");
                        userWarnings.put(userId + "_spam", 0);
                    } else {
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "⚠️ AVISO " + warnings + "/2",
                                "**Anúncio não autorizado detectado!**\n\n> 🚨 Próximo aviso: timeout de **30 minutos**.\n> 📝 Não faça propaganda sem autorização.\n> 📌 Palavra detectada: `" + keyword + "`",
                                Color.ORANGE);
                        sendWarningDM(event.getMember(), "Anúncio não autorizado (" + keyword + ")", "Aviso " + warnings + "/2 - Mensagem deletada");
                        logPunishment(event.getGuild(), event.getMember(), "Spam Anúncio", "Aviso " + warnings + "/2");
                    }
                    return;
                }
            }
        }

        // ─── COMANDOS ! (apenas admins) ───────────────────────────────────────

        if (messageRaw.startsWith("!") && !isAdmin(event)) {
            if (messageRaw.startsWith("!dmall") || messageRaw.startsWith("!verify")
                    || messageRaw.startsWith("!verifysorteio") || messageRaw.startsWith("!embed")) {
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
            if (verifyChannel == null) { event.getChannel().sendMessage("❌ Canal não encontrado! ID incorreto.").queue(); return; }

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
                            ok  -> event.getChannel().sendMessage("✅ Embed de verificação enviada com sucesso!").queue(),
                            err -> event.getChannel().sendMessage("❌ Erro ao enviar embed! Verifique permissões.").queue()
                    );
            return;
        }

        if (messageRaw.startsWith("!dmall ")) {
            sendDMToAll(event, messageRaw.substring(7), null, null);
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
            } else if (messageRaw.startsWith("!desc ")) {
                data.description = messageRaw.substring(6);
                event.getChannel().sendMessage("✅ Descrição definida.").queue();
            } else if (messageRaw.startsWith("!cor ")) {
                data.color = getColor(messageRaw.substring(5).trim());
                event.getChannel().sendMessage("✅ Cor definida.").queue();
            } else if (messageRaw.startsWith("!rodape ")) {
                data.footer = messageRaw.substring(8);
                event.getChannel().sendMessage("✅ Rodapé definido.").queue();
            } else if (messageRaw.equalsIgnoreCase("!enviar")) {
                if (data.title == null || data.description == null) {
                    event.getChannel().sendMessage("❌ Título e Descrição são obrigatórios!").queue();
                    return;
                }
                MessageEmbed embed = buildEmbed(data);
                data.embed = embed;
                event.getChannel().sendMessage("📋 **Preview:**").queue();
                event.getChannel().sendMessageEmbeds(embed).queue(s ->
                        event.getChannel().sendMessage("Digite `!confirmar` para enviar a todos.").queue());
            } else if (messageRaw.equalsIgnoreCase("!confirmar")) {
                if (data.embed != null) {
                    sendDMToAll(event, null, data.embed, null);
                    pendingEmbeds.remove(userId);
                } else {
                    event.getChannel().sendMessage("❌ Use `!enviar` primeiro para gerar a embed.").queue();
                }
            } else if (messageRaw.equalsIgnoreCase("!cancelar")) {
                pendingEmbeds.remove(userId);
                event.getChannel().sendMessage("❌ Cancelado.").queue();
            }
            return;
        }

        if (messageRaw.startsWith("!quickembed")) {
            String[] parts = messageRaw.split("\\|");
            if (parts.length >= 3) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(parts[1].trim())
                        .setDescription(parts[2].trim())
                        .setColor(getColor(parts.length > 3 ? parts[3].trim() : "azul"))
                        .setTimestamp(Instant.now());
                sendDMToAll(event, null, builder.build(), null);
            } else {
                event.getChannel().sendMessage("❌ Uso: `!quickembed | Título | Desc | Cor`").queue();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS GERAIS
    // ══════════════════════════════════════════════════════════════════════════

    private EmbedBuilder embedSucesso(String title, String desc) {
        return new EmbedBuilder().setTitle("✅ " + title).setDescription(desc).setColor(new Color(0x2ECC71));
    }

    private EmbedBuilder embedErro(String title, String desc) {
        return new EmbedBuilder().setTitle("❌ " + title).setDescription(desc).setColor(new Color(0xE74C3C));
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
        return switch (colorName.toLowerCase()) {
            case "vermelho", "red"   -> Color.RED;
            case "verde",   "green"  -> Color.GREEN;
            case "amarelo", "yellow" -> Color.YELLOW;
            case "roxo",    "purple" -> new Color(128, 0, 128);
            case "preto",   "black"  -> Color.BLACK;
            case "branco",  "white"  -> Color.WHITE;
            case "laranja", "orange" -> Color.ORANGE;
            default                  -> Color.BLUE;
        };
    }

    private void sendDMToAll(MessageReceivedEvent event, String textMessage, MessageEmbed embed, ActionRow actionRow) {
        event.getGuild().loadMembers().onSuccess(members -> {
            List<Member> targets = members.stream().filter(m -> !m.getUser().isBot()).toList();
            int total = targets.size();
            event.getChannel().sendMessage(String.format("🚀 Iniciando envio para **%d** membros... (Delay: 1.5s)", total)).queue();

            AtomicInteger sentCount       = new AtomicInteger(0);
            AtomicInteger failCount       = new AtomicInteger(0);
            AtomicInteger processedCount  = new AtomicInteger(0);

            for (int i = 0; i < targets.size(); i++) {
                Member member = targets.get(i);
                long delay = (long) i * DELAY_PER_MESSAGE_MS;

                scheduler.schedule(() ->
                                member.getUser().openPrivateChannel().queue(
                                        ch -> {
                                            var action = embed != null ? ch.sendMessageEmbeds(embed) : ch.sendMessage(textMessage);
                                            if (actionRow != null) action = action.setComponents(actionRow);
                                            action.queue(
                                                    ok  -> { sentCount.incrementAndGet(); processedCount.incrementAndGet(); logger.info("Enviado para: {}", member.getUser().getName()); },
                                                    err -> { failCount.incrementAndGet();  processedCount.incrementAndGet(); logger.warn("Falha: {}", member.getUser().getName()); }
                                            );
                                        },
                                        err -> { failCount.incrementAndGet(); processedCount.incrementAndGet(); }
                                ),
                        delay, TimeUnit.MILLISECONDS);
            }

            long reportDelay = (targets.size() * DELAY_PER_MESSAGE_MS) + 5000;
            scheduler.schedule(() -> event.getChannel().sendMessage(
                    "📊 **Relatório Final:**\n✅ Sucessos: %d\n❌ Falhas (DM Fechada): %d\n📋 Total Processado: %d / %d"
                            .formatted(sentCount.get(), failCount.get(), processedCount.get(), total)
            ).queue(), reportDelay, TimeUnit.MILLISECONDS);

        }).onError(err -> {
            event.getChannel().sendMessage("❌ Erro fatal ao carregar lista de membros!").queue();
            logger.error("Erro loadMembers: ", err);
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INNER CLASSES
    // ══════════════════════════════════════════════════════════════════════════

    private static class TeamData {
        final String nome;
        final boolean temHost;
        final String link;
        TeamData(String nome, boolean temHost, String link) {
            this.nome = nome; this.temHost = temHost; this.link = link;
        }
    }

    private static class QueueEntry {
        final String userId;
        final String username;
        final String modo;
        final boolean isHost;
        QueueEntry(String userId, String username, String modo, boolean isHost) {
            this.userId = userId; this.username = username; this.modo = modo; this.isHost = isHost;
        }
    }

    private static class EmbedData {
        String title, description, footer;
        Color color;
        MessageEmbed embed;
    }
}