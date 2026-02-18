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
    private final Map<String, List<Long>> userMessageTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Integer> userWarnings = new ConcurrentHashMap<>();

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
            "1457108663603953736"
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
            "algm amis", "alguem amis", "alguém amis", "amis", "my host", "lf my host",
            "lf host", "time bom", "meu host", "bola personalizada", "alguém pra jogar",
            "amistoso", "10 robux", "valendo robux", "vs", "versus"
    );
    private static final List<String> SPAM_KEYWORDS = Arrays.asList(
            "gravação", "gravar comigo", "participar de uma gravação", "robux gratis", "robux free",
            "free robux", "league grátis", "criar uma league", "criar liga", "impresa",
            "nacional tcs", "responder questionário", "tiktok.com", "tik tok", "vm.tiktok",
            "vt.tiktok", "busco editor", "procuro editor", "editor de fotos", "editor de video",
            "editor de vídeo", "alguém pra ownar", "alg pra ownar", "preciso de owner",
            "busco owner", "falta", "membros pra", "criar logo", "logo de league",
            "logo gratis", "logo de graça", "técnico de league", "procuro técnico", "busco técnico"
    );
    private static final List<String> PROFANITY = Arrays.asList(
            "porra", "caralho", "fdp", "buceta", "merda", "puta", "arrombado", "cuzão",
            "puta que pariu", "vai tomar no cu", "filho da puta", "vsf", "vtnc", "cu",
            "puta merda", "vai se foder", "foda-se", "foda", "cacete", "desgraça",
            "desgraçado", "otário", "idiota", "imbecil", "burro", "retardado", "mongol",
            "bosta", "corno", "viado", "gay", "bicha", "traveco", "sapatão", "vagabunda",
            "prostituta", "piranha", "vadia", "putaria", "pornografia", "punheta", "broxa",
            "penis", "vagina", "pau", "pinto", "rola", "pirocão", "piroca", "xoxota",
            "xereca", "bucetão", "rabo", "bunda", "peito", "teta", "mama"
    );

    private static final long DELAY_PER_MESSAGE_MS = 1500;

    private final Map<String, TeamData>        teams  = new ConcurrentHashMap<>();
    private final Map<String, List<QueueEntry>> queues = new ConcurrentHashMap<>();
    private final List<AgendaEntry> agendamentos = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        String token = System.getenv("TOKEN");
        System.out.println(token == null ? "TOKEN ESTA NULL!!!" : "TOKEN OK");

        try {
            // Criamos a instância do bot antes para poder chamar métodos dela depois
            Main botApp = new Main();

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
                    .addEventListeners(botApp) // Adiciona a instância que criamos
                    .build();

            jda.awaitReady();
            logger.info("Bot iniciado!");

            // --- AQUI LIGAMOS O SISTEMA DE AVISO ---
            botApp.iniciarAnuncioAutomatico(jda);
            // ---------------------------------------

            jda.updateCommands().addCommands(
                    Commands.slash("registrar-time", "Registra seu time para amistosos")
                            .addOptions(
                                    new OptionData(OptionType.STRING,  "nome",           "Nome do seu time",                           true),
                                    new OptionData(OptionType.BOOLEAN, "seu-host",        "Você tem servidor privado (host próprio)?",  true),
                                    new OptionData(OptionType.STRING,  "link-servidor",   "Link do seu servidor privado (se tiver)",    false)
                            ),
                    Commands.slash("fila", "Entrar na fila de amistosos")
                            .addOptions(
                                    new OptionData(OptionType.STRING, "modo", "Modo de jogo", true)
                                            .addChoice("5v5","5v5").addChoice("6v6","6v6")
                                            .addChoice("7v7","7v7").addChoice("4v4","4v4"),
                                    new OptionData(OptionType.BOOLEAN, "seu-host", "Você será o host?", true)
                            ),
                    Commands.slash("sair-fila",   "Sair da fila de amistosos"),
                    Commands.slash("fila-status", "Ver quem está na fila"),
                    Commands.slash("agendar", "Agendar um amistoso")
                            .addOptions(
                                    new OptionData(OptionType.STRING, "modo", "Modo de jogo", true)
                                            .addChoice("5v5","5v5").addChoice("6v6","6v6")
                                            .addChoice("7v7","7v7").addChoice("4v4","4v4"),
                                    new OptionData(OptionType.STRING, "horario", "Horário (ex: 19:30)", true)
                            ),
                    Commands.slash("agendas", "Ver todos os amistosos agendados")
            ).queue();

            logger.info("Slash commands registrados!");
        } catch (Exception e) {
            logger.error("Erro ao iniciar o bot", e);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "registrar-time" -> handleRegistrar(event);
            case "fila"           -> handleFila(event);
            case "sair-fila"      -> handleSairFila(event);
            case "fila-status"    -> handleFilaStatus(event);
            case "agendar"        -> handleAgendar(event);
            case "agendas"        -> handleAgendas(event);
        }
    }

    public void iniciarAnuncioAutomatico(net.dv8tion.jda.api.JDA jda) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 1. Escolher um canal aleatório da lista de amistosos
                if (AMIS_CHANNELS.isEmpty()) return;
                String randomChannelId = AMIS_CHANNELS.get(new Random().nextInt(AMIS_CHANNELS.size()));
                TextChannel channel = jda.getTextChannelById(randomChannelId);

                if (channel != null) {
                    // 2. Criar a Embed de Dica
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("💡  Dica: Encontre partidas mais rápido!")
                            .setDescription("Cansado de ficar mandando msg procurando time?\n\n" +
                                    "**USE O SISTEMA AUTOMÁTICO DO BOT:**\n" +
                                    "1️⃣  Use `/registrar-time` (só uma vez)\n" +
                                    "2️⃣  Use `/fila` e escolha o modo (5v5, 6v6...)\n\n" +
                                    "✅ **O bot te avisa na DM quando achar adversário!**\n" +
                                    "⚡ _É automático, rápido e organiza o host._")
                            .setColor(new Color(0x3498DB)) // Azul claro
                            .setThumbnail(CUSTOM_ICON)
                            .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                            .setTimestamp(Instant.now());

                    // 3. Enviar
                    channel.sendMessageEmbeds(embed.build()).queue(
                            s -> logger.info("Anuncio automatico enviado no canal: " + channel.getName()),
                            e -> logger.warn("Nao consegui enviar anuncio no canal " + randomChannelId)
                    );
                }
            } catch (Exception e) {
                logger.error("Erro no sistema de anuncio automatico", e);
            }
        }, 0, 1, TimeUnit.HOURS); // 0 de atraso inicial (manda na hora), repete a cada 1 Hora
    }

    private void handleRegistrar(SlashCommandInteractionEvent event) {
        String userId   = event.getUser().getId();
        String nome     = event.getOption("nome", OptionMapping::getAsString);
        boolean temHost = Boolean.TRUE.equals(event.getOption("seu-host", OptionMapping::getAsBoolean));
        String link     = event.getOption("link-servidor", OptionMapping::getAsString);

        if (nome == null || nome.isBlank()) {
            event.replyEmbeds(embedErro("Nome inválido", "Digite um nome para o seu time.").build())
                    .setEphemeral(true).queue(); return;
        }

        TeamData anterior = teams.get(userId);
        if (temHost && (link == null || link.isBlank())) {
            if (anterior != null && anterior.link != null && !anterior.link.isBlank()) {
                link = anterior.link;
            } else {
                event.replyEmbeds(embedErro("Link necessário",
                                "Você marcou que tem host. Informe o link do seu servidor privado.").build())
                        .setEphemeral(true).queue(); return;
            }
        }

        boolean atualizando = anterior != null;
        teams.put(userId, new TeamData(nome, temHost, link));

        String desc = atualizando
                ? "Time atualizado! Se estava na fila, saia e entre novamente." : "";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle((atualizando ? "✏️" : "✅") + "  Time " + (atualizando ? "Atualizado" : "Registrado") + "!")
                .setDescription(desc)
                .addField("🏆  Time", "**" + nome + "**", true)
                .addField("🏠  Host", temHost ? "Sim ✅" : "Não ❌", true)
                .setColor(atualizando ? new Color(0x3498DB) : new Color(0x2ECC71))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Use /fila para entrar na fila!", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        if (temHost && link != null) embed.addField("🔗  Servidor", link, false);
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void handleFila(SlashCommandInteractionEvent event) {
        String userId  = event.getUser().getId();
        String modo    = event.getOption("modo", OptionMapping::getAsString);
        boolean isHost = Boolean.TRUE.equals(event.getOption("seu-host", OptionMapping::getAsBoolean));

        if (!teams.containsKey(userId)) {
            event.replyEmbeds(embedErro("Time não registrado",
                            "Use `/registrar-time` primeiro!").build())
                    .setEphemeral(true).queue(); return;
        }
        if (isInQueue(userId)) {
            event.replyEmbeds(embedErro("Já na fila",
                            "Você já está na fila de **" + getQueueMode(userId) + "**!\nUse `/sair-fila` para sair.").build())
                    .setEphemeral(true).queue(); return;
        }

        TeamData time = teams.get(userId);
        String avisoHost = (isHost && (time.link == null || time.link.isBlank()))
                ? "\n\n⚠️ Sem link registrado — combine o servidor no privado com o adversário." : "";

        QueueEntry entry = new QueueEntry(userId, event.getUser().getAsTag(), modo, isHost);
        queues.computeIfAbsent(modo, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);

        QueueEntry[] match = tryMatch(modo);

        if (match != null) {
            EmbedBuilder confirmEmbed = new EmbedBuilder()
                    .setTitle("⚽  Match na hora!")
                    .setDescription("Você entrou e já havia adversário!\n📩 **Verifique sua DM** para os detalhes.")
                    .addField("🏆  Seu time", "**" + time.nome + "**", true)
                    .addField("🏠  Host", isHost ? "Sim ✅" : "Não ❌", true)
                    .setColor(new Color(0xF1C40F))
                    .setThumbnail(CUSTOM_ICON)
                    .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(confirmEmbed.build()).setEphemeral(true).queue();
            processMatch(event, match[0], match[1], modo);
        } else {
            EmbedBuilder confirmEmbed = new EmbedBuilder()
                    .setTitle("🔍  Na fila de " + modo + "!")
                    .setDescription("Aguardando adversário...\nUse `/sair-fila` para cancelar." + avisoHost)
                    .addField("🏆  Seu time", "**" + time.nome + "**", true)
                    .addField("🏠  Host", isHost ? "Sim ✅" : "Não ❌", true)
                    .setColor(new Color(0x9B59B6))
                    .setThumbnail(CUSTOM_ICON)
                    .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(confirmEmbed.build()).setEphemeral(true).queue();
        }
    }

    private void processMatch(SlashCommandInteractionEvent event, QueueEntry p1, QueueEntry p2, String modo) {
        logger.info("MATCH: {} vs {} modo {}", p1.username, p2.username, modo);

        TeamData t1 = teams.get(p1.userId);
        TeamData t2 = teams.get(p2.userId);

        QueueEntry hostEntry  = p1.isHost ? p1 : (p2.isHost ? p2 : p1);
        QueueEntry guestEntry = hostEntry.userId.equals(p1.userId) ? p2 : p1;
        TeamData   hostTeam   = teams.get(hostEntry.userId);

        String nomeT1   = t1 != null ? t1.nome : p1.username;
        String nomeT2   = t2 != null ? t2.nome : p2.username;
        String nomeHost = hostTeam != null ? hostTeam.nome : hostEntry.username;
        String linkInfo = (hostTeam != null && hostTeam.link != null && !hostTeam.link.isBlank())
                ? hostTeam.link : "*(sem link — combinem no privado)*";

        EmbedBuilder embedCanal = new EmbedBuilder()
                .setTitle("🏆  AMISTOSO ENCONTRADO!")
                .setDescription(
                        "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n" +
                                "## ⚽  " + nomeT1 + "  ×  " + nomeT2 + "\n" +
                                "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n\n" +
                                "🎮  Modo: **" + modo + "**\n" +
                                "🏠  Host: **" + nomeHost + "**\n" +
                                "🔗  " + linkInfo + "\n\n" +
                                "> 📩 Verifiquem a DM para detalhes!"
                )
                .setColor(new Color(0xF1C40F))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        String targetChannelId = switch (modo) {
            case "6v6" -> "1449070508816728198";
            case "5v5" -> "1449070534934401044";
            case "7v7" -> "1449070445327421682";
            case "4v4" -> "1457070154226602208";
            default -> event.getChannel().getId();
        };

        TextChannel matchChannel = event.getJDA().getTextChannelById(targetChannelId);

        if (matchChannel != null) {
            matchChannel.sendMessage("🎯 <@" + p1.userId + "> <@" + p2.userId + ">")
                    .setEmbeds(embedCanal.build())
                    .queue(ok -> logger.info("Match no canal OK"), err -> logger.warn("Erro canal: {}", err.getMessage()));
        } else {
            event.getChannel().sendMessage("🎯 <@" + p1.userId + "> <@" + p2.userId + ">")
                    .setEmbeds(embedCanal.build())
                    .queue(ok -> logger.info("Match no canal OK"), err -> logger.warn("Erro canal: {}", err.getMessage()));
        }

        EmbedBuilder dmHost = new EmbedBuilder()
                .setTitle("🏆  AMISTOSO ENCONTRADO!")
                .setDescription(
                        "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n" +
                                "## ⚽  " + nomeT1 + "  ×  " + nomeT2 + "\n" +
                                "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n\n" +
                                "🎮  Modo: **" + modo + "**\n" +
                                "🏠  Você é o **HOST!**\n" +
                                "🔗  Seu servidor: " + linkInfo + "\n" +
                                "👤  Adversário: <@" + guestEntry.userId + "> — **" + nomeT2 + "**\n\n" +
                                "> 💬 Aguarde o adversário ou chame no privado!"
                )
                .setColor(new Color(0x2ECC71))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        EmbedBuilder dmGuest = new EmbedBuilder()
                .setTitle("🏆  AMISTOSO ENCONTRADO!")
                .setDescription(
                        "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n" +
                                "## ⚽  " + nomeT1 + "  ×  " + nomeT2 + "\n" +
                                "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n\n" +
                                "🎮  Modo: **" + modo + "**\n" +
                                "🏠  Host: **" + nomeHost + "**\n" +
                                "🔗  Servidor: " + linkInfo + "\n" +
                                "👤  Chame o host: <@" + hostEntry.userId + "> — **" + nomeHost + "**\n\n" +
                                "> 💬 Chame o host no privado para confirmar!"
                )
                .setColor(new Color(0x3498DB))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        sendDM(event.getJDA(), hostEntry.userId,  dmHost.build());
        sendDM(event.getJDA(), guestEntry.userId, dmGuest.build());
    }

    private void sendDM(net.dv8tion.jda.api.JDA jda, String userId, MessageEmbed embed) {
        jda.retrieveUserById(userId).queue(
                user -> user.openPrivateChannel().queue(
                        ch -> ch.sendMessageEmbeds(embed).queue(
                                ok  -> logger.info("DM OK para {}", user.getAsTag()),
                                err -> logger.warn("DM bloqueada para {}", user.getAsTag())
                        ),
                        err -> logger.warn("Não abriu DM com {}", userId)
                ),
                err -> logger.warn("Usuário não encontrado: {}", userId)
        );
    }

    private void handleSairFila(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        boolean removed = queues.values().stream()
                .anyMatch(list -> list.removeIf(e -> e.userId.equals(userId)));

        if (removed) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("✅  Saiu da fila!")
                            .setDescription("Você saiu da fila com sucesso.")
                            .setColor(new Color(0x2ECC71)).setThumbnail(CUSTOM_ICON)
                            .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON).build())
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
                            .setTitle("🔍  Fila vazia")
                            .setDescription("Nenhum jogador na fila agora.\nUse `/fila` para entrar!")
                            .setColor(new Color(0x3498DB)).setThumbnail(CUSTOM_ICON)
                            .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON).build())
                    .setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔍  Status das Filas")
                .setColor(new Color(0x9B59B6))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("🏠 = Host  |  🎮 = Sem host", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        for (String modo : List.of("5v5","6v6","7v7","4v4")) {
            List<QueueEntry> lista = queues.getOrDefault(modo, Collections.emptyList());
            if (!lista.isEmpty()) {
                StringJoiner sb = new StringJoiner("\n");
                for (QueueEntry e : lista) {
                    TeamData t = teams.get(e.userId);
                    sb.add((e.isHost ? "🏠" : "🎮") + "  **" + (t != null ? t.nome : e.username) + "**");
                }
                embed.addField("⚽  " + modo + "  (" + lista.size() + " na fila)", sb.toString(), false);
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
                            "Use `/registrar-time` primeiro!").build())
                    .setEphemeral(true).queue(); return;
        }
        if (horario == null || !horario.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            event.replyEmbeds(embedErro("Horário inválido", "Use o formato `HH:MM`. Ex: `19:30`").build())
                    .setEphemeral(true).queue(); return;
        }

        TeamData time = teams.get(userId);
        agendamentos.add(new AgendaEntry(userId, event.getUser().getAsTag(), time.nome, modo, horario));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📅  Amistoso Agendado!")
                .setDescription(
                        "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n" +
                                "<@" + userId + "> está procurando adversário!\n" +
                                "**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n\n" +
                                "🏆  Time: **" + time.nome + "**\n" +
                                "🎮  Modo: **" + modo + "**\n" +
                                "🕐  Horário: **" + horario + "**\n" +
                                "🏠  Host: " + (time.temHost ? "Sim ✅" : "Não ❌") + "\n" +
                                (time.temHost && time.link != null ? "🔗  " + time.link + "\n" : "") +
                                "\n> 💬 Entre em contato no privado para confirmar!"
                )
                .setColor(new Color(0xF1C40F))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        event.getChannel()
                .sendMessage("📣 <@" + userId + ">")
                .setEmbeds(embed.build())
                .queue();

        event.replyEmbeds(new EmbedBuilder()
                        .setTitle("✅  Agendado!")
                        .setDescription("Seu amistoso foi anunciado no canal!\nAguarde alguém entrar em contato.")
                        .setColor(new Color(0x2ECC71)).setThumbnail(CUSTOM_ICON)
                        .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON).build())
                .setEphemeral(true).queue();

        sendDM(event.getJDA(), userId, new EmbedBuilder()
                .setTitle("📅  Amistoso Agendado!")
                .setDescription(
                        "Seu amistoso foi anunciado no canal!\n\n" +
                                "🎮  Modo: **" + modo + "**\n" +
                                "🕐  Horário: **" + horario + "**\n\n" +
                                "> Aguarde alguém entrar em contato no privado."
                )
                .setColor(new Color(0xF1C40F)).setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                .setTimestamp(Instant.now()).build());
    }

    private void handleAgendas(SlashCommandInteractionEvent event) {
        if (agendamentos.isEmpty()) {
            event.replyEmbeds(embedErro("Nenhum Agendamento", "Não há amistosos agendados no momento.").build())
                    .setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📅  Amistosos Agendados")
                .setColor(new Color(0x9B59B6))
                .setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON)
                .setTimestamp(Instant.now());

        for (AgendaEntry agenda : agendamentos) {
            embed.addField("⚽ " + agenda.modo + " às " + agenda.horario,
                    "**Time:** " + agenda.teamName + "\n**Líder:** <@" + agenda.userId + ">", false);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

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
        List<QueueEntry> list = queues.computeIfAbsent(modo, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (list) {
            if (list.size() < 2) return null;

            QueueEntry host = null, guest = null;
            for (QueueEntry e : list) {
                if (e.isHost  && host  == null) host  = e;
                if (!e.isHost && guest == null) guest = e;
                if (host != null && guest != null) break;
            }

            QueueEntry p1, p2;
            if (host != null && guest != null && !host.userId.equals(guest.userId)) {
                p1 = host; p2 = guest;
            } else {
                p1 = list.get(0); p2 = null;
                for (int i = 1; i < list.size(); i++) {
                    if (!list.get(i).userId.equals(p1.userId)) { p2 = list.get(i); break; }
                }
                if (p2 == null) return null;
            }
            list.remove(p1); list.remove(p2);
            return new QueueEntry[]{p1, p2};
        }
    }

    private boolean isAdmin(MessageReceivedEvent event) {
        return event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    private void sendWarningDM(Member member, String reason, String action) {
        member.getUser().openPrivateChannel().queue(ch -> {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("⚠️  Aviso de Moderação — PAFO")
                    .setDescription("Você recebeu uma advertência no servidor.")
                    .addField("📋  Motivo", reason, false)
                    .addField("⚡  Ação Tomada", action, false)
                    .addField("📌  Dica", "Leia as regras do servidor para evitar futuras punições.", false)
                    .setColor(Color.ORANGE).setThumbnail(CUSTOM_ICON)
                    .setFooter("Sistema AutoMod • PAFO", CUSTOM_ICON).setTimestamp(Instant.now());
            ch.sendMessageEmbeds(embed.build()).queue(
                    ok  -> logger.info("DM aviso para {}", member.getUser().getName()),
                    err -> logger.warn("DM bloqueada: {}", member.getUser().getName())
            );
        }, err -> logger.warn("Sem DM: {}", member.getUser().getName()));
    }

    private void logPunishment(Guild guild, Member member, String reason, String action) {
        TextChannel ch = guild.getTextChannelById(LOG_CHANNEL_ID);
        if (ch == null) return;
        ch.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("🛡️  Registro de Punição")
                .addField("👤  Usuário", member.getAsMention() + " (" + member.getId() + ")", false)
                .addField("📋  Motivo", reason, false)
                .addField("⚡  Ação", action, false)
                .setColor(Color.RED).setThumbnail(member.getUser().getAvatarUrl())
                .setFooter("Log Automático • PAFO", CUSTOM_ICON).setTimestamp(Instant.now()).build()).queue();
    }

    private void sendTemporaryWarning(TextChannel channel, Member member, String title, String description, Color color) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle(title).setDescription(member.getAsMention() + " " + description)
                        .setColor(color).setThumbnail(CUSTOM_ICON)
                        .setFooter("Sistema AutoMod • PAFO", CUSTOM_ICON).setTimestamp(Instant.now()).build())
                .queue(msg -> scheduler.schedule(() -> msg.delete().queue(), 60, TimeUnit.SECONDS));
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("roblox_modal")) return;
        var input = event.getValue("roblox_username");
        String robloxUser = input != null ? input.getAsString() : "Desconhecido";
        Member member = event.getMember();
        if (member == null) { event.reply("❌ Membro não encontrado.").setEphemeral(true).queue(); return; }

        String newNick = member.getEffectiveName() + " (@" + robloxUser + ")";
        if (newNick.length() > 32) newNick = newNick.substring(0, 32);
        String finalNick = newNick;
        member.modifyNickname(finalNick).queue(
                ok  -> addRoleAndReply(event, member, finalNick, true),
                err -> addRoleAndReply(event, member, finalNick, false)
        );
    }

    private void addRoleAndReply(ModalInteractionEvent event, Member member, String newNick, boolean nickChanged) {
        Role r = event.getGuild().getRoleById(VERIFIED_ROLE_ID);
        if (r != null) event.getGuild().addRoleToMember(member, r).queue();
        event.reply(nickChanged
                        ? "✅ **VERIFICADO!** 🎉\n\n**Novo apelido:** " + newNick + "\n\nAcesso liberado! 🚀"
                        : "✅ **VERIFICADO!** 🎉\n\n(Não consegui alterar o apelido — falta de permissão.)\nAltere manualmente para: " + newNick)
                .setEphemeral(true).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("verify_button")) {
            Member member = event.getMember();
            if (member == null) { event.reply("❌ Erro.").setEphemeral(true).queue(); return; }
            Role r = event.getGuild().getRoleById(VERIFIED_ROLE_ID);
            if (r != null && member.getRoles().contains(r)) {
                event.reply("✅ Você já está verificado!").setEphemeral(true).queue(); return;
            }
            TextInput input = TextInput.create("roblox_username","Nome de usuário do Roblox", TextInputStyle.SHORT)
                    .setPlaceholder("Digite seu usuário do Roblox").setRequired(true).setMaxLength(20).build();
            event.replyModal(Modal.create("roblox_modal","🎮 Verificação Roblox").addActionRow(input).build()).queue();

        } else if (event.getComponentId().equals("go_sorteio")) {
            event.reply("🎁 **Link do Sorteio:**\n" + SORTEIO_CANAL_LINK + "\n\nBoa sorte! 🍀").setEphemeral(true).queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot() || event.getMember() == null) return;

        String raw     = event.getMessage().getContentRaw();
        String lower   = raw.toLowerCase();
        String userId  = event.getAuthor().getId();
        String chanId  = event.getChannel().getId();

        if (!isAdmin(event)) {
            long now = System.currentTimeMillis();
            userMessageTimestamps.putIfAbsent(userId, new ArrayList<>());
            List<Long> times = userMessageTimestamps.get(userId);
            times.add(now); times.removeIf(t -> now - t > 5000);
            if (times.size() >= 5) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                        "⚠️ SPAM DETECTADO", "**Pare de enviar mensagens tão rápido!**\n> 5+ msgs em 5 segundos.", Color.ORANGE);
                sendWarningDM(event.getMember(), "Spam (5+ em 5s)", "Mensagem deletada");
                logPunishment(event.getGuild(), event.getMember(), "Spam velocidade", "Deletado");
                return;
            }

            for (String word : PROFANITY) {
                if (lower.matches(".*\\b" + word + "\\b.*")) {
                    event.getMessage().delete().queue();
                    int w = userWarnings.getOrDefault(userId, 0) + 1;
                    userWarnings.put(userId, w);
                    if (w >= 3) {
                        event.getMember().timeoutFor(Duration.ofHours(1)).queue();
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "🚫 SILENCIADO 1H", "**3 avisos atingidos!**\n> Timeout de **1 hora**.", Color.RED);
                        sendWarningDM(event.getMember(), "Linguagem inadequada (3x)", "Timeout 1h");
                        logPunishment(event.getGuild(), event.getMember(), "Linguagem (3x)", "Timeout 1h");
                        userWarnings.put(userId, 0);
                    } else {
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "⚠️ AVISO " + w + "/3", "**Linguagem inadequada!**\n> No 3º aviso: timeout 1h.", Color.ORANGE);
                        sendWarningDM(event.getMember(), "Linguagem inadequada", "Aviso " + w + "/3");
                        logPunishment(event.getGuild(), event.getMember(), "Linguagem", "Aviso " + w + "/3");
                    }
                    return;
                }
            }

            for (String g : GFX_KEYWORDS) {
                if (lower.contains(g) && !chanId.equals(GFX_CHANNEL_ID)) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ GFX NO CANAL ERRADO", "Use o canal correto: <#" + GFX_CHANNEL_ID + ">", Color.RED);
                    sendWarningDM(event.getMember(), "GFX em canal errado", "Deletado");
                    logPunishment(event.getGuild(), event.getMember(), "GFX canal errado", "Deletado");
                    return;
                }
            }

            for (String a : AMIS_KEYWORDS) {
                if (lower.contains(a)) {
                    if (chanId.equals(FA_ALLOWED_CHANNEL)) continue;
                    if (!AMIS_CHANNELS.contains(chanId)) {
                        event.getMessage().delete().queue();
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "❌ AMIS NO CANAL ERRADO",
                                "Use: <#1449070534934401044> <#1449070508816728198> <#1449070445327421682> <#1457070154226602208>",
                                Color.RED);
                        sendWarningDM(event.getMember(), "AMIS canal errado", "Deletado");
                        logPunishment(event.getGuild(), event.getMember(), "AMIS canal errado", "Deletado");
                        return;
                    }
                }
            }

            boolean hasRoblox = lower.matches(".*(roblox\\.com|ro\\.blox\\.com|rblx\\.co).*");
            boolean hasOther  = lower.matches(".*(https?://|www\\.|discord\\.gg/)\\S+.*") && !hasRoblox;

            if (hasRoblox && chanId.equals("1453095863823110184")) return;

            if (hasRoblox && chanId.equals(PENEIRA_CHANNEL)) {
                boolean ok = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(OLHEIRO_ROLE_ID));
                if (!ok) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ APENAS OLHEIROS", "Só **Olheiros** podem enviar links aqui.", Color.RED);
                    sendWarningDM(event.getMember(), "Link sem Olheiro", "Deletado");
                    logPunishment(event.getGuild(), event.getMember(), "Link s/ Olheiro", "Deletado");
                }
                return;
            }

            if (hasRoblox && chanId.equals(SCRIM_CHANNEL)) {
                boolean ok = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(SCRIM_HOSTER_ROLE_ID));
                if (!ok) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ APENAS SCRIM HOSTER", "Só **Scrim Hoster** podem enviar links aqui.", Color.RED);
                    sendWarningDM(event.getMember(), "Link sem Scrim Hoster", "Deletado");
                    logPunishment(event.getGuild(), event.getMember(), "Link s/ Scrim Hoster", "Deletado");
                }
                return;
            }

            if (hasOther && !ALLOWED_LINK_CHANNELS.contains(chanId)) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                        "❌ LINKS NÃO PERMITIDOS", "Use: <#1449070133778714738> <#1449112362912186389>", Color.RED);
                sendWarningDM(event.getMember(), "Link não autorizado", "Deletado");
                logPunishment(event.getGuild(), event.getMember(), "Link proibido", "Deletado");
                return;
            }

            if (hasRoblox && !chanId.equals("1453095863823110184") && !chanId.equals(PENEIRA_CHANNEL)
                    && !chanId.equals(SCRIM_CHANNEL) && !ALLOWED_LINK_CHANNELS.contains(chanId)) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                        "❌ LINK ROBLOX AQUI NÃO",
                        "Canais permitidos:\n• <#1453095863823110184>\n• <#" + PENEIRA_CHANNEL + "> (Olheiros)\n• <#" + SCRIM_CHANNEL + "> (Scrim Hoster)",
                        Color.RED);
                sendWarningDM(event.getMember(), "Link Roblox canal errado", "Deletado");
                logPunishment(event.getGuild(), event.getMember(), "Link Roblox errado", "Deletado");
                return;
            }

            if ((lower.contains("f/a") || lower.matches(".*\\bf/a\\b.*")) && !chanId.equals(FA_ALLOWED_CHANNEL)) {
                event.getMessage().delete().queue();
                sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                        "❌ F/A NO CANAL ERRADO", "Use: <#" + FA_ALLOWED_CHANNEL + ">", Color.RED);
                sendWarningDM(event.getMember(), "F/A canal errado", "Deletado");
                logPunishment(event.getGuild(), event.getMember(), "F/A canal errado", "Deletado");
                return;
            }

            if (lower.contains("peneira para") || lower.contains("peneira pro")) {
                boolean ok = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals(OLHEIRO_ROLE_ID));
                if (!ok) {
                    event.getMessage().delete().queue();
                    sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                            "❌ APENAS OLHEIROS", "Só **Olheiros** podem divulgar peneiras.", Color.RED);
                    sendWarningDM(event.getMember(), "Peneira sem Olheiro", "Deletado");
                    logPunishment(event.getGuild(), event.getMember(), "Peneira s/ Olheiro", "Deletado");
                    return;
                }
            }

            for (String kw : SPAM_KEYWORDS) {
                if (lower.contains(kw)) {
                    event.getMessage().delete().queue();
                    int w = userWarnings.getOrDefault(userId + "_spam", 0) + 1;
                    userWarnings.put(userId + "_spam", w);
                    if (w >= 2) {
                        event.getMember().timeoutFor(Duration.ofMinutes(30)).queue();
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "🚫 SILENCIADO 30min", "**2º aviso spam!**\n> Timeout 30 min.\n> Palavra: `" + kw + "`", Color.RED);
                        sendWarningDM(event.getMember(), "Spam anúncio (2x) - " + kw, "Timeout 30min");
                        logPunishment(event.getGuild(), event.getMember(), "Spam anúncio 2x", "Timeout 30min");
                        userWarnings.put(userId + "_spam", 0);
                    } else {
                        sendTemporaryWarning(event.getChannel().asTextChannel(), event.getMember(),
                                "⚠️ AVISO 1/2 SPAM", "**Anúncio não autorizado!**\n> Próximo: timeout 30min.\n> Palavra: `" + kw + "`", Color.ORANGE);
                        sendWarningDM(event.getMember(), "Anúncio (" + kw + ")", "Aviso 1/2");
                        logPunishment(event.getGuild(), event.getMember(), "Spam anúncio", "Aviso 1/2");
                    }
                    return;
                }
            }
        }

        if (!isAdmin(event)) return;

        if (raw.equalsIgnoreCase("!enquete-staff")) {
            List<String> candidatos = Arrays.asList(
                    "1256770517550235730",
                    "1319821237278474312",
                    "1456815801054138471",
                    "1427435337495351366",
                    "1326384626788204669"
            );

            List<String> emojis = Arrays.asList("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣");

            StringBuilder desc = new StringBuilder();
            desc.append("**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n");
            desc.append("Vote no melhor staff da semana!\n");
            desc.append("**━━━━━━━━━━━━━━━━━━━━━━━━━━━━**\n\n");

            for (int i = 0; i < candidatos.size(); i++) {
                desc.append(emojis.get(i)).append("  <@").append(candidatos.get(i)).append(">\n");
            }

            desc.append("\n> 🗳️ Reaja com o emoji do seu favorito!");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🏅  STAFF DA SEMANA — Vote!")
                    .setDescription(desc.toString())
                    .setColor(new Color(0xF1C40F))
                    .setThumbnail(CUSTOM_ICON)
                    .setFooter("Bot PAFO • Enquete Semanal", CUSTOM_ICON)
                    .setTimestamp(Instant.now());

            event.getChannel().sendMessageEmbeds(embed.build()).queue(msg -> {
                for (String emoji : emojis) {
                    msg.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode(emoji)).queue();
                }
            });
        }

        if (raw.equalsIgnoreCase("!verify")) {
            TextChannel ch = event.getGuild().getTextChannelById(VERIFY_CHANNEL_ID);
            if (ch == null) { event.getChannel().sendMessage("❌ Canal não encontrado!").queue(); return; }
            ch.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("🔐  Verificação do Servidor")
                            .setDescription(
                                    "**Bem-vindo(a) ao PAFO!** 👋\n\n" +
                                            "> Para ter acesso completo, verifique-se.\n\n" +
                                            "**📋 Como:**\n1. Clique em **✅ Verificar**\n2. Digite seu user do Roblox\n3. Acesso liberado! 🎉\n\n" +
                                            "**Seu apelido:** SeuNome (@RobloxUser)"
                            )
                            .setColor(new Color(87, 242, 135)).setThumbnail(CUSTOM_ICON)
                            .setFooter("Sistema de Verificação • PAFO", CUSTOM_ICON).setTimestamp(Instant.now()).build())
                    .setActionRow(Button.success("verify_button", "✅ Verificar"))
                    .queue(ok -> event.getChannel().sendMessage("✅ Embed enviada!").queue(),
                            err -> event.getChannel().sendMessage("❌ Erro de permissão.").queue());
        }

        if (raw.equalsIgnoreCase("!verifysorteio")) {
            EmbedBuilder e = new EmbedBuilder()
                    .setTitle("🎁  SORTEIO DE OLHEIRO GRÁTIS!")
                    .setDescription(
                            "## 🔥 SORTEIO ESPECIAL 🔥\n\n" +
                                    "Estamos sorteando **OLHEIRO DE GRAÇA**!\n\n" +
                                    "> 1️⃣ Clique em **Ir para Verificação**\n" +
                                    "> 2️⃣ Verifique-se\n> 3️⃣ Clique em **Participar do Sorteio**\n\n" +
                                    "🍀 Boa sorte!"
                    )
                    .setColor(new Color(255, 215, 0)).setThumbnail(CUSTOM_ICON)
                    .setFooter("PAFO • Sorteio", CUSTOM_ICON).setTimestamp(Instant.now());

            ActionRow row = ActionRow.of(
                    Button.link(VERIFY_CHANNEL_LINK, "✅ Ir para Verificação"),
                    Button.primary("go_sorteio", "🎁 Participar do Sorteio")
            );
            sendDMToAll(event, null, e.build(), row);
        }
    }

    private void sendDMToAll(MessageReceivedEvent event, String text, MessageEmbed embed, ActionRow row) {
        event.getGuild().loadMembers().onSuccess(members -> {
            var targets = members.stream().filter(m -> !m.getUser().isBot()).toList();
            event.getChannel().sendMessage("🚀 Enviando para **" + targets.size() + "** membros...").queue();
            AtomicInteger sent = new AtomicInteger(), fail = new AtomicInteger();

            for (int i = 0; i < targets.size(); i++) {
                Member m = targets.get(i);
                scheduler.schedule(() -> m.getUser().openPrivateChannel().queue(
                        ch -> {
                            var action = embed != null ? ch.sendMessageEmbeds(embed) : ch.sendMessage(text);
                            if (row != null) action = action.setComponents(row);
                            action.queue(ok -> sent.incrementAndGet(), err -> fail.incrementAndGet());
                        },
                        err -> fail.incrementAndGet()
                ), (long) i * DELAY_PER_MESSAGE_MS, TimeUnit.MILLISECONDS);
            }

            long delay = (long) targets.size() * DELAY_PER_MESSAGE_MS + 5000;
            scheduler.schedule(() -> event.getChannel().sendMessage(
                    "📊 **Relatório:** ✅ " + sent.get() + "  ❌ " + fail.get()
            ).queue(), delay, TimeUnit.MILLISECONDS);
        });
    }

    private EmbedBuilder embedErro(String title, String desc) {
        return new EmbedBuilder().setTitle("❌  " + title).setDescription(desc)
                .setColor(new Color(0xE74C3C)).setThumbnail(CUSTOM_ICON)
                .setFooter("Bot Amistosos • PAFO", CUSTOM_ICON);
    }

    private static class TeamData {
        final String nome; final boolean temHost; final String link;
        TeamData(String nome, boolean temHost, String link) {
            this.nome = nome; this.temHost = temHost; this.link = link;
        }
    }

    private static class QueueEntry {
        final String userId, username, modo; final boolean isHost;
        QueueEntry(String userId, String username, String modo, boolean isHost) {
            this.userId = userId; this.username = username; this.modo = modo; this.isHost = isHost;
        }
    }

    private static class AgendaEntry {
        final String userId, username, teamName, modo, horario;
        AgendaEntry(String userId, String username, String teamName, String modo, String horario) {
            this.userId = userId; this.username = username; this.teamName = teamName;
            this.modo = modo; this.horario = horario;
        }
    }
}