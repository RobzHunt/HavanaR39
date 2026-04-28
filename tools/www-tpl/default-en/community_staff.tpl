<div class="habblet-container">
    <div class="cbb clearfix blue ">
        <h2 class="title">Equipe do Hotel</h2>

        {% for group in staff %}

        <div class="staff-group {{ group.color }}">
            <h3>{{ group.name }}</h3>

            {% for user in group.users %}
            <div class="staff-card">

                <div class="avatar">
                    <img src="/avatarimage.php?figure={{ user.look }}&size=m">
                    <div class="badge">{{ user.badge }}</div>
                </div>

                <div class="info">
                    <b>{{ user.username }}</b>
                    <span class="rank">{{ user.rank_name }}</span>
                    <p>{{ user.motto }}</p>

                    <span class="status {{ user.online }}">
                        {{ user.online_text }}
                    </span>
                </div>

            </div>
            {% endfor %}

        </div>

        {% endfor %}
    </div>
</div>
